#include "telemetria.h"

#include <Arduino.h>
#include <WiFi.h>
#include <atomic>
#include <mbedtls/ssl.h>
#include <mqtt_client.h>
#include <time.h>
#include <tuple>
#include "ca_hivemq.h"
#include "secrets.h"

namespace {
constexpr unsigned long INTERVALO_RECONEXAO_MS = 15000;
struct Amostra {
  char payload[TAMANHO_PAYLOAD];
  unsigned long coletadaEm;
};
QueueHandle_t amostras;
esp_mqtt_client_handle_t cliente;
std::atomic<bool> conectado{false};
mbedtls_x509_crt raiz;

std::tuple<int, int, int, int, int, int> camposUtc(const mbedtls_x509_time& t) {
  return std::make_tuple(t.year, t.mon, t.day, t.hour, t.min, t.sec);
}

// O mbedTLS pré-compilado do Arduino-ESP32 2.0.17 desabilita HAVE_TIME_DATE.
// Acrescenta os erros de validade aos de cadeia e hostname calculados pela biblioteca.
int verificarDatas(void*, mbedtls_x509_crt* cert, int, uint32_t* flags) {
  time_t instante = time(nullptr);
  struct tm utc;
  if (!gmtime_r(&instante, &utc)) return MBEDTLS_ERR_X509_FATAL_ERROR;
  auto agora = std::make_tuple(utc.tm_year + 1900, utc.tm_mon + 1, utc.tm_mday,
                              utc.tm_hour, utc.tm_min, utc.tm_sec);
  if (agora < camposUtc(cert->valid_from)) *flags |= MBEDTLS_X509_BADCERT_FUTURE;
  if (agora > camposUtc(cert->valid_to)) *flags |= MBEDTLS_X509_BADCERT_EXPIRED;
  return 0;
}

esp_err_t configurarCA(void* contexto) {
  auto tls = static_cast<mbedtls_ssl_config*>(contexto);
  mbedtls_ssl_conf_ca_chain(tls, &raiz, nullptr);
  mbedtls_ssl_conf_verify(tls, verificarDatas, nullptr);
  return ESP_OK;
}

void eventoMqtt(void*, esp_event_base_t, int32_t id, void* dados) {
  auto evento = static_cast<esp_mqtt_event_handle_t>(dados);
  switch (id) {
    case MQTT_EVENT_CONNECTED:
      conectado = true;
      Serial.println("[MQTT] Conectado via TLS.");
      break;
    case MQTT_EVENT_DISCONNECTED:
      conectado = false;
      Serial.println("[MQTT] Desconectado. Reconexão automática.");
      break;
    case MQTT_EVENT_PUBLISHED:
      Serial.printf("[MQTT] PUBACK recebido (id=%d).\n", evento->msg_id);
      break;
    case MQTT_EVENT_ERROR:
      Serial.printf("[MQTT] Erro tipo=%d, TLS=0x%x, CONNACK=%d.\n",
                    evento->error_handle->error_type, evento->error_handle->esp_tls_last_esp_err,
                    evento->error_handle->connect_return_code);
      break;
  }
}

void iniciarCliente() {
  static char id[48];
  snprintf(id, sizeof(id), "greenhouse-esp32-estufa1-%012llx",
           static_cast<unsigned long long>(ESP.getEfuseMac()));
  esp_mqtt_client_config_t cfg{};
  cfg.host = MQTT_HOST;
  cfg.port = 8883;
  cfg.transport = MQTT_TRANSPORT_OVER_SSL;
  cfg.crt_bundle_attach = configurarCA;
  cfg.client_id = id;
  cfg.username = MQTT_USERNAME;
  cfg.password = MQTT_PASSWORD;
  cfg.reconnect_timeout_ms = INTERVALO_RECONEXAO_MS;
  cfg.network_timeout_ms = 5000;
  cliente = esp_mqtt_client_init(&cfg);
  if (cliente && esp_mqtt_client_register_event(cliente, MQTT_EVENT_ANY, eventoMqtt, nullptr) == ESP_OK &&
      esp_mqtt_client_start(cliente) == ESP_OK) return;

  if (cliente) esp_mqtt_client_destroy(cliente);
  cliente = nullptr;
  Serial.println("[MQTT] Falha ao iniciar o cliente. Nova tentativa em 15 s.");
}

void manterRede(void*) {
  mbedtls_x509_crt_init(&raiz);
  if (mbedtls_x509_crt_parse(&raiz, reinterpret_cast<const unsigned char*>(CA_HIVEMQ),
                             sizeof(CA_HIVEMQ)) != 0) {
    mbedtls_x509_crt_free(&raiz);
    Serial.println("[TLS] CA inválida. Telemetria desabilitada.");
    vTaskDelete(nullptr);
    return;
  }
  WiFi.persistent(false);
  WiFi.mode(WIFI_STA);
  WiFi.setAutoReconnect(false);
  WiFi.begin(WIFI_SSID, WIFI_PASSWORD);
  Serial.println("[Wi-Fi] Conectando.");
  unsigned long ultimaTentativaWifi = millis();
  unsigned long ultimaTentativaMqtt = millis() - INTERVALO_RECONEXAO_MS;
  bool tinhaWifi = false;
  for (;;) {
    const unsigned long agora = millis();
    const bool temWifi = WiFi.status() == WL_CONNECTED;
    if (temWifi && !tinhaWifi) {
      configTime(0, 0, "pool.ntp.org", "time.google.com");
      Serial.println("[Wi-Fi] Conectado. Sincronizando horário para TLS.");
    } else if (!temWifi && agora - ultimaTentativaWifi >= INTERVALO_RECONEXAO_MS) {
      WiFi.reconnect();
      ultimaTentativaWifi = agora;
      Serial.println("[Wi-Fi] Tentando reconectar.");
    }
    tinhaWifi = temWifi;

    // SNTP ajusta o relógio em segundo plano. O TLS verifica a validade completa da cadeia.
    if (temWifi && !cliente && time(nullptr) >= 1704067200 &&
        agora - ultimaTentativaMqtt >= INTERVALO_RECONEXAO_MS) {
      ultimaTentativaMqtt = agora;
      iniciarCliente();
    }
    Amostra amostra;
    if (xQueueReceive(amostras, &amostra, pdMS_TO_TICKS(250)) == pdTRUE &&
        conectado && millis() - amostra.coletadaEm < INTERVALO_SENSORES_MS) {
      // Limita a uma publicação aguardando confirmação durante falhas de rede.
      if (esp_mqtt_client_get_outbox_size(cliente) > 0) {
        Serial.println("[MQTT] PUBACK pendente. Leitura atual somente local.");
        continue;
      }
      int id = esp_mqtt_client_enqueue(cliente, TOPICO_ESTUFA, amostra.payload, 0, 1, 0, true);
      if (id < 0) Serial.println("[MQTT] Falha ao enfileirar a leitura.");
      else Serial.printf("[MQTT] Leitura enfileirada (QoS 1, id=%d).\n", id);
    }
  }
}
}  // namespace

void iniciarTelemetria() {
  if (!WIFI_SSID[0] || !MQTT_HOST[0] || !MQTT_USERNAME[0] || !MQTT_PASSWORD[0]) {
    Serial.println("[Rede] Preencha secrets.h para habilitar a telemetria.");
    return;
  }
  amostras = xQueueCreate(1, sizeof(Amostra));
  if (amostras && xTaskCreate(manterRede, "telemetria", 6144, nullptr, 1, nullptr) == pdPASS) return;
  if (amostras) vQueueDelete(amostras);
  amostras = nullptr;
  Serial.println("[Rede] Memória insuficiente para telemetria.");
}

void publicarLeituraReal(const char (&payload)[TAMANHO_PAYLOAD]) {
  if (!conectado) {
    Serial.println("[MQTT] Offline. Leitura somente local.");
    return;
  }
  Amostra amostra{};
  strlcpy(amostra.payload, payload, sizeof(amostra.payload));
  amostra.coletadaEm = millis();
  xQueueOverwrite(amostras, &amostra);
}
