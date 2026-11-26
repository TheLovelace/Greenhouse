/*
 * =====================================================================
 *  GREENHOUSE - Monitoramento IoT (ESP32)
 *  Grupo Lovelace · Qualidade e Teste de Software
 *
 *  Caso de uso : Configurar cuidados e monitoramento de plantas
 *                (RF15, RF16 e RF17)
 *  Roteiro     : Planilha "Roteiro de Teste - Sistema Greenhouse" (CTs #01 a #11)
 *                #01 a #06 = caminhos do GFC (diagrama de atividade)
 *                #07 a #11 = sequências S1 a S5 do GE (diagrama de transição de estados)
 *
 *  Este arquivo é a adaptação para C++ do backend em Kotlin:
 *    - LeituraIotService  -> processarMensagem(), classificar(),
 *                            converterUmidadeSolo(), iniciarMonitoramento()
 *    - FilaLeituraIot     -> classe FilaLeituraIot (fila em memória)
 *    - NivelComplexidade  -> nivelComplexidade(), precoMinimo()
 *    - CondicaoAmbiental  -> enum Condicao
 *
 *  Como usar (Serial Monitor a 115200):
 *    - Ao iniciar, lê sensores físicos e publica as leituras válidas via MQTT.
 *    - Menu de testes locais:  1..6 = um teste do GFC | t = todos os 6 |
 *                               a..e = uma sequência do GE (S1..S5) | g = todas as 5 |
 *                               r = resumo geral dos 11 testes |
 *                               l = modo sensores (DHT11 + potenciômetro) |
 *                               m = menu
 *
 *  Pinos: DHT11=15 | Solo(potenciômetro)=34 | LED verde=26 |
 *         LED amarelo=27 | LED vermelho=14 | Buzzer=25 | OLED I2C=21/22
 * =====================================================================
 */

#include <Arduino.h>
#include <math.h>
#include <stdarg.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <functional>
#include "DHTesp.h"
#include "telemetria.h"

#define USAR_LCD 1  // coloque 0 se não for usar o display LCD
#if USAR_LCD
#include <Wire.h>
#include <Adafruit_GFX.h>
#include <Adafruit_SSD1306.h>
#endif

// ---------------------------------------------------------------------
// 0. CONFIGURAÇÃO DE HARDWARE
// ---------------------------------------------------------------------
const int PIN_DHT = 15;
const int PIN_SOLO = 34;
const int PIN_LED_VERDE = 26;
const int PIN_LED_AMARELO = 27;
const int PIN_LED_VERMELHO = 14;
const int PIN_BUZZER = 25;

const unsigned long PAUSA_ENTRE_TESTES_MS = 1000;


DHTesp dht;
#if USAR_LCD
#define SCREEN_WIDTH 128
#define SCREEN_HEIGHT 64

#define SDA_PIN 21
#define SCL_PIN 22

Adafruit_SSD1306 display(
  SCREEN_WIDTH,
  SCREEN_HEIGHT,
  &Wire,
  -1
);
#endif

// ---------------------------------------------------------------------
// TIPOS (ficam no topo de propósito: a IDE do Arduino cria sozinha as
// declarações de todas as funções e elas precisam enxergar estes tipos)
// ---------------------------------------------------------------------
const int SEM_INT = -1;
#define SEM_VALOR NAN

enum Condicao { ABAIXO_DO_IDEAL, IDEAL, ACIMA_DO_IDEAL, NAO_CONFIGURADO };
enum Parametro { TEMPERATURA, UMIDADE_AR, UMIDADE_SOLO };
enum Resultado {
  SAUDAVEL,
  ESPECIE_NAO_ENCONTRADA,
  CONFIG_INVALIDA,
  PRECO_INCOMPATIVEL,
  DADOS_IOT_INVALIDOS,
  ALERTA_GERADO
};

// Cuidados e parâmetros de monitoramento informados pelo administrador (RF15)
struct Configuracao {
  const char* especie;
  int intervaloRegaDias;       // SEM_INT = ausente
  const char* luminosidade;    // nullptr = ausente
  double tempMin, tempMax;     // SEM_VALOR = ausente
  double arMin, arMax;
  double soloMin, soloMax;
  bool monitoraTemperatura;
  bool monitoraUmidadeAr;
  bool monitoraUmidadeSolo;
  bool categoriasDefinidas;
  double precoProduto;         // preço do produto associado à espécie
};

struct DispositivoIot {
  const char* topico;
  bool ativo;
  int calibracaoSeco;    // leitura bruta com o solo seco
  int calibracaoUmido;   // leitura bruta com o solo encharcado
};

struct Ocorrencia {
  Parametro parametro;
  double valorMedido;
  Condicao condicao;
  double faixaMin;
  double faixaMax;
};

struct AvaliacaoLeitura {
  bool aceita;       // false = leitura rejeitada (ver motivo)
  bool avaliada;     // true = havia configuração/alerta ativo para avaliar
  char motivo[110];
  double temperatura;
  double umidadeAr;
  double umidadeSolo;
  Ocorrencia ocorrencias[3];
  int totalOcorrencias;
};

struct Esperado {
  Resultado resultado;
  bool salvaConfiguracao;   // a configuração deve ser salva?
  bool comparaParametros;   // o sistema deve comparar os dados com os parâmetros?
  bool geraAlerta;          // deve gerar alerta e notificar o administrador?
};

struct Cenario {
  const char* id;
  const char* nome;
  const char* caminho;       // caminho independente do GFC
  const char* nos;           // decisões percorridas
  Configuracao config;       // entrada do administrador
  const char* payload;       // mensagem do dispositivo IoT (nullptr = não enviada)
  Esperado esperado;
  const char* saidaEsperada; // texto da coluna "Saída Esperada" da planilha
};

struct Execucao {
  Resultado resultado;
  bool salvou;
  bool comparou;
  bool alertou;
  bool notificou;
  char mensagem[330];
};

// Estados finais da Leitura IoT (DTE/GE) e cenários dos testes derivados do GE (sequências S1 a S5)
enum EstadoLeitura {
  LEITURA_REJEITADA,
  LEITURA_SEM_MONITORAMENTO,
  LEITURA_CONDICAO_IDEAL,
  LEITURA_FORA_DO_LIMITE
};

struct CenarioGE {
  const char* id;           // número do caso de teste na planilha
  const char* sequencia;    // S1 a S5 (Quadro 2b)
  const char* nome;
  const char* transicoes;   // sequência de transições esperada no GE
  const char* topico;       // tópico MQTT em que o dispositivo publica
  const char* payload;      // mensagem do dispositivo IoT
  bool alertaAtivo;         // existe alerta ativo (com espécie ativa) para o dispositivo?
  EstadoLeitura esperado;   // estado final esperado
  const char* saidaEsperada;
};

// Port de FilaLeituraIot (no Kotlin é uma interface; aqui, fila em memória).
// A fila simulada não possui transporte de rede.
class FilaLeituraIot {
 public:
  typedef std::function<void(const char*)> Receptor;

  FilaLeituraIot() : total(0) {}

  void assinar(const char* topico, Receptor aoReceber) {
    if (total < MAX_ASSINATURAS) {
      assinaturas[total].topico = topico;
      assinaturas[total].receptor = aoReceber;
      total++;
    }
  }

  void publicar(const char* topico, const char* payload) {
    for (int i = 0; i < total; i++) {
      if (strcmp(assinaturas[i].topico, topico) == 0) assinaturas[i].receptor(payload);
    }
  }

 private:
  static const int MAX_ASSINATURAS = 4;
  struct Assinatura {
    const char* topico;
    Receptor receptor;
  };
  Assinatura assinaturas[MAX_ASSINATURAS];
  int total;
};

void imprimir(const char* formato, ...) {
  char buf[320];
  va_list args;
  va_start(args, formato);
  vsnprintf(buf, sizeof(buf), formato, args);
  va_end(args);
  Serial.println(buf);
}

// ---------------------------------------------------------------------
// 1. TIPOS BÁSICOS
//    Em Kotlin/Java os campos podem ser nulos (Int?, Double?).
//    Em C++ usamos: SEM_INT (-1) para inteiros e NAN para decimais.
// ---------------------------------------------------------------------
static inline bool semValor(double v) { return v != v; }

const char* nomeCondicao(Condicao c) {
  switch (c) {
    case ABAIXO_DO_IDEAL: return "ABAIXO_DO_IDEAL";
    case IDEAL: return "IDEAL";
    case ACIMA_DO_IDEAL: return "ACIMA_DO_IDEAL";
    default: return "NAO_CONFIGURADO";
  }
}

const char* nomeParametro(Parametro p) {
  switch (p) {
    case TEMPERATURA: return "temperatura";
    case UMIDADE_AR: return "umidade do ar";
    default: return "umidade do solo";
  }
}

const char* unidadeParametro(Parametro p) { return p == TEMPERATURA ? "°C" : "%"; }

const char* nomeResultado(Resultado r) {
  switch (r) {
    case SAUDAVEL: return "SAUDAVEL";
    case ESPECIE_NAO_ENCONTRADA: return "ESPECIE_NAO_ENCONTRADA";
    case CONFIG_INVALIDA: return "CONFIG_INVALIDA";
    case PRECO_INCOMPATIVEL: return "PRECO_INCOMPATIVEL";
    case DADOS_IOT_INVALIDOS: return "DADOS_IOT_INVALIDOS";
    default: return "ALERTA_GERADO";
  }
}

// Texto curto (máx. 16 caracteres, sem acento) para o display LCD
const char* rotuloLcd(Resultado r) {
  switch (r) {
    case SAUDAVEL: return "PLANTA SAUDAVEL";
    case ESPECIE_NAO_ENCONTRADA: return "ESPECIE NAO ACH.";
    case CONFIG_INVALIDA: return "CONFIG INVALIDA";
    case PRECO_INCOMPATIVEL: return "PRECO INCOMPAT.";
    case DADOS_IOT_INVALIDOS: return "DADOS IOT INVAL.";
    default: return "ALERTA GERADO!";
  }
}

// ---------------------------------------------------------------------
// 2. REGRAS DE NEGÓCIO (port de NivelComplexidade e LeituraIotService)
// ---------------------------------------------------------------------

// Amplitude = máximo - mínimo; sem valor se algum dos limites estiver ausente
double amplitude(double minimo, double maximo) {
  if (semValor(minimo) || semValor(maximo)) return SEM_VALOR;
  return maximo - minimo;
}

// Port direto de nivelComplexidade(): devolve 0 (BAIXA), 1 (MEDIA) ou 2 (ALTA)
int nivelComplexidade(int intervaloRegaDias, double amplitudeTemperatura, double amplitudeAr,
                      double amplitudeSolo) {
  int pontosRega = 0;

  // Avaliacao da frequencia de rega
  if (intervaloRegaDias == SEM_INT) {
    pontosRega = 0;
  } else if (intervaloRegaDias <= 2) {
    pontosRega = 2;
  } else if (intervaloRegaDias <= 7) {
    pontosRega = 1;
  } else {
    pontosRega = 0;
  }

  int pontosTemperatura = 0;

  // Avaliacao da amplitude termica
  if (semValor(amplitudeTemperatura)) {
    pontosTemperatura = 0;
  } else if (amplitudeTemperatura < 5.0) {
    pontosTemperatura = 2;
  } else if (amplitudeTemperatura < 10.0) {
    pontosTemperatura = 1;
  } else {
    pontosTemperatura = 0;
  }

  int pontosUmidade = 0;

  // Avaliacao da umidade do ar
  if (semValor(amplitudeAr)) {
    pontosUmidade = pontosUmidade + 0;
  } else if (amplitudeAr < 20.0) {
    pontosUmidade = pontosUmidade + 1;
  } else {
    pontosUmidade = pontosUmidade + 0;
  }

  // Avaliacao da umidade do solo
  if (semValor(amplitudeSolo)) {
    pontosUmidade = pontosUmidade + 0;
  } else if (amplitudeSolo < 20.0) {
    pontosUmidade = pontosUmidade + 1;
  } else {
    pontosUmidade = pontosUmidade + 0;
  }

  // Pontuacao total dos cuidados
  int total = pontosRega + pontosTemperatura + pontosUmidade;

  // Classificacao da complexidade
  int nivel = 0;

  if (total <= 1) {
    nivel = 0;
  } else if (total <= 3) {
    nivel = 1;
  } else {
    nivel = 2;
  }

  return nivel;
}

const char* nomeNivel(int nivel) {
  switch (nivel) {
    case 0: return "BAIXA";
    case 1: return "MEDIA";
    default: return "ALTA";
  }
}

// NivelComplexidade.precoMinimo (BAIXA = 0, MEDIA = 30, ALTA = 80)
double precoMinimo(int nivel) {
  switch (nivel) {
    case 0: return 0.0;
    case 1: return 30.0;
    default: return 80.0;
  }
}

// precoCompativel(): o preço deve corresponder à complexidade do tratamento (RN02)
bool precoCompativel(int nivel, double preco) { return preco >= precoMinimo(nivel); }

bool foraDoLimite(Condicao c) { return c == ABAIXO_DO_IDEAL || c == ACIMA_DO_IDEAL; }

// classificar(): compara o valor medido com a faixa ideal da espécie
Condicao classificar(double minimo, double maximo, double valor) {
  if (semValor(minimo) && semValor(maximo)) return NAO_CONFIGURADO;
  if (!semValor(minimo) && valor < minimo) return ABAIXO_DO_IDEAL;
  if (!semValor(maximo) && valor > maximo) return ACIMA_DO_IDEAL;
  return IDEAL;
}

// converterUmidadeSolo(): leitura bruta do sensor -> porcentagem (0 a 100)
// %% = (seco - bruto) * 100 / (seco - umido). Falso se a calibração for inválida.
bool converterUmidadeSolo(int seco, int umido, int bruto, double &percentual) {
  if (seco == SEM_INT || umido == SEM_INT || seco == umido) return false;
  double p = (double)(seco - bruto) * 100.0 / (double)(seco - umido);
  if (p < 0.0) p = 0.0;
  if (p > 100.0) p = 100.0;
  percentual = round(p * 100.0) / 100.0;  // 2 casas decimais
  return true;
}

// ---------------------------------------------------------------------
// 3. CONFIGURAÇÃO, DISPOSITIVO IoT, FILA E PROCESSAMENTO DE MENSAGEM
// ---------------------------------------------------------------------

const DispositivoIot DISPOSITIVOS[] = {
  {TOPICO_ESTUFA, true, 3200, 1200},
};
const int TOTAL_DISPOSITIVOS = sizeof(DISPOSITIVOS) / sizeof(DISPOSITIVOS[0]);

// Estado do "banco de dados" da simulação
Configuracao configSalva;
bool configSalvaValida = false;
AvaliacaoLeitura ultimaAvaliacao;

const DispositivoIot* buscarDispositivo(const char* topico) {
  for (int i = 0; i < TOTAL_DISPOSITIVOS; i++) {
    if (strcmp(DISPOSITIVOS[i].topico, topico) == 0) return &DISPOSITIVOS[i];
  }
  return nullptr;
}

// Lê um campo numérico de um JSON plano, ex.: {"temperatura":22.5}. NAN se ausente.
double lerCampo(const char* json, const char* chave) {
  char busca[40];
  snprintf(busca, sizeof(busca), "\"%s\"", chave);
  const char* p = strstr(json, busca);
  if (p == nullptr) return SEM_VALOR;
  p += strlen(busca);
  while (*p == ' ') p++;
  if (*p != ':') return SEM_VALOR;
  p++;
  while (*p == ' ') p++;
  char* fim = nullptr;
  double valor = strtod(p, &fim);
  if (fim == p) return SEM_VALOR;
  return valor;
}

bool jsonBemFormado(const char* texto) {
  if (texto == nullptr) return false;
  size_t n = strlen(texto);
  while (n > 0 && texto[n - 1] == ' ') n--;
  const char* ini = texto;
  while (*ini == ' ') ini++;
  return n > 0 && *ini == '{' && texto[n - 1] == '}';
}

AvaliacaoLeitura rejeitar(const char* motivo) {
  AvaliacaoLeitura a;
  memset(&a, 0, sizeof(a));
  a.aceita = false;
  strncpy(a.motivo, motivo, sizeof(a.motivo) - 1);
  return a;
}

void avaliarParametro(bool monitora, Parametro parametro, double minimo, double maximo, double valor,
                      AvaliacaoLeitura &a) {
  if (!monitora) return;
  Condicao condicao = classificar(minimo, maximo, valor);
  if (foraDoLimite(condicao) && a.totalOcorrencias < 3) {
    Ocorrencia &o = a.ocorrencias[a.totalOcorrencias++];
    o.parametro = parametro;
    o.valorMedido = valor;
    o.condicao = condicao;
    o.faixaMin = minimo;
    o.faixaMax = maximo;
  }
}

// Port de LeituraIotService.processarMensagem() (RF16 e RF17)
AvaliacaoLeitura processarMensagem(const char* topico, const char* payload) {
  const DispositivoIot* dispositivo = buscarDispositivo(topico);
  if (dispositivo == nullptr) return rejeitar("Dispositivo IoT não cadastrado para o tópico informado");
  if (!dispositivo->ativo) return rejeitar("O dispositivo IoT não está autorizado a enviar dados");

  if (!jsonBemFormado(payload)) return rejeitar("Payload da leitura IoT inválido");
  double temperatura = lerCampo(payload, "temperatura");
  double umidadeAr = lerCampo(payload, "umidadeAr");
  double bruto = lerCampo(payload, "umidadeSoloRaw");
  if (semValor(temperatura)) return rejeitar("Campo 'temperatura' ausente ou inválido");
  if (semValor(umidadeAr)) return rejeitar("Campo 'umidadeAr' ausente ou inválido");
  if (semValor(bruto)) return rejeitar("Campo 'umidadeSoloRaw' ausente ou inválido");

  if (umidadeAr < 0.0 || umidadeAr > 100.0) return rejeitar("A umidade do ar deve estar entre 0 e 100");
  if (bruto < 0.0) return rejeitar("A leitura bruta de umidade do solo não pode ser negativa");

  double umidadeSolo = 0.0;
  if (!converterUmidadeSolo(dispositivo->calibracaoSeco, dispositivo->calibracaoUmido, (int)bruto, umidadeSolo)) {
    return rejeitar("O dispositivo IoT não possui calibração de umidade do solo válida");
  }

  AvaliacaoLeitura a;
  memset(&a, 0, sizeof(a));
  a.aceita = true;
  a.temperatura = temperatura;
  a.umidadeAr = umidadeAr;
  a.umidadeSolo = umidadeSolo;

  // Avalia contra a configuração salva (equivale aos alertas ativos da espécie)
  if (configSalvaValida) {
    a.avaliada = true;
    avaliarParametro(configSalva.monitoraTemperatura, TEMPERATURA, configSalva.tempMin, configSalva.tempMax,
                     temperatura, a);
    avaliarParametro(configSalva.monitoraUmidadeAr, UMIDADE_AR, configSalva.arMin, configSalva.arMax,
                     umidadeAr, a);
    avaliarParametro(configSalva.monitoraUmidadeSolo, UMIDADE_SOLO, configSalva.soloMin, configSalva.soloMax,
                     umidadeSolo, a);
  }
  return a;
}

// Port de receberComSeguranca(): erro numa mensagem não derruba o consumidor
void receberComSeguranca(const char* topico, const char* payload) {
  ultimaAvaliacao = processarMensagem(topico, payload);
  if (!ultimaAvaliacao.aceita) {
    imprimir("   [IoT] Leitura descartada no tópico %s: %s", topico, ultimaAvaliacao.motivo);
  }
}

FilaLeituraIot fila;

// Port de iniciarMonitoramento(): assina o tópico de cada dispositivo ativo
int iniciarMonitoramento() {
  int assinados = 0;
  for (int i = 0; i < TOTAL_DISPOSITIVOS; i++) {
    if (!DISPOSITIVOS[i].ativo) continue;
    const char* topico = DISPOSITIVOS[i].topico;
    fila.assinar(topico, [topico](const char* payload) { receberComSeguranca(topico, payload); });
    assinados++;
  }
  return assinados;
}

// ---------------------------------------------------------------------
// 4. CADASTRO DE ESPÉCIES E VALIDAÇÃO DA CONFIGURAÇÃO (RF15)
// ---------------------------------------------------------------------
const char* const ESPECIES_CADASTRADAS[] = {"Violeta Africana", "Orquidea Phalaenopsis", "Samambaia Americana"};
const int TOTAL_ESPECIES = sizeof(ESPECIES_CADASTRADAS) / sizeof(ESPECIES_CADASTRADAS[0]);

bool especieCadastrada(const char* nome) {
  if (nome == nullptr) return false;
  for (int i = 0; i < TOTAL_ESPECIES; i++) {
    if (strcmp(ESPECIES_CADASTRADAS[i], nome) == 0) return true;
  }
  return false;
}

void anexarErro(char* destino, size_t tamanho, const char* erro) {
  if (destino[0] != '\0') strncat(destino, "; ", tamanho - strlen(destino) - 1);
  strncat(destino, erro, tamanho - strlen(destino) - 1);
}

// Verifica uma faixa [min, max]; limite100 = true restringe a 0..100 (umidades)
void validarFaixa(char* erros, size_t tamanho, const char* nome, double minimo, double maximo, bool limite100) {
  char msg[80];
  if (semValor(minimo) || semValor(maximo)) {
    snprintf(msg, sizeof(msg), "faixa de %s ausente", nome);
    anexarErro(erros, tamanho, msg);
  } else if (minimo > maximo) {
    snprintf(msg, sizeof(msg), "faixa de %s inválida (mínimo maior que máximo)", nome);
    anexarErro(erros, tamanho, msg);
  } else if (limite100 && (minimo < 0.0 || maximo > 100.0)) {
    snprintf(msg, sizeof(msg), "faixa de %s fora de 0 a 100", nome);
    anexarErro(erros, tamanho, msg);
  }
}

// N8: "Configuração completa?" - devolve true se estiver tudo preenchido e válido
bool validarConfiguracao(const Configuracao &c, char* erros, size_t tamanho) {
  erros[0] = '\0';

  if (c.intervaloRegaDias == SEM_INT) {
    anexarErro(erros, tamanho, "frequência de rega ausente");
  } else if (c.intervaloRegaDias < 1) {
    anexarErro(erros, tamanho, "frequência de rega inválida (mínimo 1 dia)");
  }
  if (c.luminosidade == nullptr || c.luminosidade[0] == '\0') anexarErro(erros, tamanho, "luminosidade ausente");

  validarFaixa(erros, tamanho, "temperatura", c.tempMin, c.tempMax, false);

  if (!c.monitoraTemperatura && !c.monitoraUmidadeAr && !c.monitoraUmidadeSolo) {
    anexarErro(erros, tamanho, "nenhum parâmetro de monitoramento definido");
  }
  if (c.monitoraUmidadeAr) validarFaixa(erros, tamanho, "umidade do ar", c.arMin, c.arMax, true);
  if (c.monitoraUmidadeSolo) validarFaixa(erros, tamanho, "umidade do solo", c.soloMin, c.soloMax, true);
  if (!c.categoriasDefinidas) anexarErro(erros, tamanho, "categorias não definidas");

  return erros[0] == '\0';
}

// ---------------------------------------------------------------------
// 5. CASOS DE TESTE (planilha "Roteiro de Teste - Sistema Greenhouse")
// ---------------------------------------------------------------------
// Payloads simulados para os testes locais
const char* const PAYLOAD_NORMAL = "{\"temperatura\":22.0,\"umidadeAr\":65.0,\"umidadeSoloRaw\":2100}";
const char* const PAYLOAD_INCOMPLETO = "{\"temperatura\":22.0,\"umidadeAr\":65.0}";
const char* const PAYLOAD_FORA_DA_FAIXA = "{\"temperatura\":30.0,\"umidadeAr\":65.0,\"umidadeSoloRaw\":2800}";

// Violeta Africana: rega 2 dias (2 pts) + amplitude térmica 4 °C (2 pts)
// + umidade do ar 10 pp (1 pt) + solo 10 pp (1 pt) = 6 pontos -> complexidade ALTA
// -> preço mínimo R$ 80,00.
const Cenario CENARIOS[6] = {
  {"#01", "Fluxo Principal", "C1", "N2=Sim; N8=Sim; N11=Sim; N16=Sim; N19=Sim",
   {"Violeta Africana", 2, "Luz indireta brilhante", 20.0, 24.0, 60.0, 70.0, 50.0, 60.0, true, true, true, true, 120.0},
   PAYLOAD_NORMAL,
   {SAUDAVEL, true, true, false},
   "O sistema valida e salva as configurações, compara os dados recebidos e categoriza a planta como saudável, "
   "sem gerar alerta."},

  {"#02", "Fluxo de Exceção 1 - Espécie não cadastrada", "C2", "N2=Não",
   {"Samambaia Fantasma", 2, "Luz indireta brilhante", 20.0, 24.0, 60.0, 70.0, 50.0, 60.0, true, true, true, true, 120.0},
   nullptr,
   {ESPECIE_NAO_ENCONTRADA, false, false, false},
   "O sistema informa que a espécie não foi encontrada e encerra o fluxo, sem permitir a configuração."},

  {"#03", "Fluxo de Exceção 2 - Configuração incompleta ou inválida", "C3", "N2=Sim; N8=Não",
   {"Violeta Africana", 2, nullptr, SEM_VALOR, SEM_VALOR, 60.0, 70.0, 50.0, 60.0, true, true, true, false, 120.0},
   nullptr,
   {CONFIG_INVALIDA, false, false, false},
   "O sistema informa os campos ausentes ou parâmetros inválidos e não salva a configuração."},

  {"#04", "Fluxo de Exceção 3 - Preço incompatível", "C4", "N2=Sim; N8=Sim; N11=Não",
   {"Violeta Africana", 2, "Luz indireta brilhante", 20.0, 24.0, 60.0, 70.0, 50.0, 60.0, true, true, true, true, 45.0},
   nullptr,
   {PRECO_INCOMPATIVEL, false, false, false},
   "O sistema informa a divergência entre preço e complexidade do tratamento e não salva as configurações."},

  {"#05", "Fluxo de Exceção 4 - Dados IoT inválidos ou ausentes", "C5", "N2=Sim; N8=Sim; N11=Sim; N16=Não",
   {"Violeta Africana", 2, "Luz indireta brilhante", 20.0, 24.0, 60.0, 70.0, 50.0, 60.0, true, true, true, true, 120.0},
   PAYLOAD_INCOMPLETO,
   {DADOS_IOT_INVALIDOS, true, false, false},
   "O sistema registra a falha ou ausência de dados e encerra a avaliação sem comparar os parâmetros nem "
   "categorizar a planta."},

  {"#06", "Fluxo Alternativo 1 - Condição inadequada da planta", "C6", "N2=Sim; N8=Sim; N11=Sim; N16=Sim; N19=Não",
   {"Violeta Africana", 2, "Luz indireta brilhante", 20.0, 24.0, 60.0, 70.0, 50.0, 60.0, true, true, true, true, 120.0},
   PAYLOAD_FORA_DA_FAIXA,
   {ALERTA_GERADO, true, true, true},
   "O sistema identifica e categoriza a condição inadequada, gera um alerta de cuidado necessário e notifica "
   "o administrador."},
};
const int TOTAL_CENARIOS = 6;

// ---------------------------------------------------------------------
// 5b. CASOS DE TESTE DERIVADOS DO GRAFO DE ESTADOS (Quadro 2b: S1 a S5)
//     Uma sequência independente do GE por caso de teste (#07 a #11).
//     Faixa ideal da espécie: temperatura 20 a 24 °C.
// ---------------------------------------------------------------------
const char* const TOPICO_INEXISTENTE = "greenhouse/estufa99/leitura";
const char* const PAYLOAD_AR_100_01 = "{\"temperatura\":22.0,\"umidadeAr\":100.01,\"umidadeSoloRaw\":2100}";
const char* const PAYLOAD_TEMP_24_00 = "{\"temperatura\":24.0,\"umidadeAr\":65.0,\"umidadeSoloRaw\":2100}";
const char* const PAYLOAD_TEMP_24_01 = "{\"temperatura\":24.01,\"umidadeAr\":65.0,\"umidadeSoloRaw\":2100}";

const CenarioGE CENARIOS_GE[5] = {
  {"#07", "S1", "Tópico sem dispositivo cadastrado", "T0 → T1 → F1", TOPICO_INEXISTENTE, PAYLOAD_NORMAL, true,
   LEITURA_REJEITADA,
   "Leitura rejeitada: dispositivo IoT não cadastrado para o tópico. Nada é salvo."},
  {"#08", "S2", "Umidade do ar acima de 100 % (valor limite 100,01)", "T0 → T2 → T3 → F1", TOPICO_ESTUFA,
   PAYLOAD_AR_100_01, true, LEITURA_REJEITADA,
   "Leitura rejeitada: a umidade do ar deve estar entre 0 e 100. Nada é salvo."},
  {"#09", "S3", "Leitura válida sem alerta ativo", "T0 → T2 → T4 → T5 → F2", TOPICO_ESTUFA, PAYLOAD_NORMAL, false,
   LEITURA_SEM_MONITORAMENTO,
   "Leitura salva, mas sem alerta ativo: não há avaliação (sem monitoramento)."},
  {"#10", "S4", "Temperatura no limite superior da faixa (24,00)", "T0 → T2 → T4 → T6 → T7 → F3", TOPICO_ESTUFA,
   PAYLOAD_TEMP_24_00, true, LEITURA_CONDICAO_IDEAL,
   "Leitura salva e avaliada: todos os parâmetros dentro da faixa (condição ideal), sem ocorrência."},
  {"#11", "S5", "Temperatura logo acima do limite superior (24,01)", "T0 → T2 → T4 → T6 → T8 → F4", TOPICO_ESTUFA,
   PAYLOAD_TEMP_24_01, true, LEITURA_FORA_DO_LIMITE,
   "Leitura salva e avaliada: temperatura ACIMA_DO_IDEAL, ocorrência de alerta identificada."},
};
const int TOTAL_CENARIOS_GE = 5;

// ---------------------------------------------------------------------
// 6. HARDWARE: LCD, LEDs E BUZZER
// ---------------------------------------------------------------------
void lcdMostrar(const char* linha1, const char* linha2) {
#if USAR_LCD
  display.clearDisplay();
  display.setTextColor(SSD1306_WHITE);
  display.setTextSize(1);
  display.setTextWrap(false);

  // Primeira linha
  display.setCursor(0, 0);
  display.println(linha1);

  // Segunda linha
  display.setCursor(0, 18);
  display.println(linha2);

  // Atualiza o conteúdo do OLED
  display.display();
#else
  (void)linha1;
  (void)linha2;
#endif
}

void ledsApagar() {
  digitalWrite(PIN_LED_VERDE, LOW);
  digitalWrite(PIN_LED_AMARELO, LOW);
  digitalWrite(PIN_LED_VERMELHO, LOW);
}

// Onda quadrada no buzzer (funciona em qualquer versão do core ESP32)
void bipe(int frequenciaHz, int duracaoMs) {
  long meioPeriodoUs = 500000L / frequenciaHz;
  long ciclos = (long)frequenciaHz * duracaoMs / 1000;
  for (long i = 0; i < ciclos; i++) {
    digitalWrite(PIN_BUZZER, HIGH);
    delayMicroseconds(meioPeriodoUs);
    digitalWrite(PIN_BUZZER, LOW);
    delayMicroseconds(meioPeriodoUs);
  }
}

// Verde = planta saudável | Amarelo = fluxo encerrado por validação | Vermelho + buzzer = alerta
void sinalizar(Resultado r) {
  ledsApagar();
  if (r == SAUDAVEL) {
    digitalWrite(PIN_LED_VERDE, HIGH);
    bipe(1500, 120);
  } else if (r == ALERTA_GERADO) {
    for (int i = 0; i < 3; i++) {
      digitalWrite(PIN_LED_VERMELHO, HIGH);
      bipe(880, 250);
      digitalWrite(PIN_LED_VERMELHO, LOW);
      delay(150);
    }
    digitalWrite(PIN_LED_VERMELHO, HIGH);
  } else {
    digitalWrite(PIN_LED_AMARELO, HIGH);
    bipe(300, 300);
  }
}

// ---------------------------------------------------------------------
// 7. EXECUÇÃO DO FLUXO DO CASO DE USO (diagrama de atividade)
// ---------------------------------------------------------------------
void faixaTexto(char* destino, size_t tamanho, double minimo, double maximo, const char* unidade) {
  if (semValor(minimo) || semValor(maximo)) {
    snprintf(destino, tamanho, "ausente");
  } else {
    snprintf(destino, tamanho, "%.1f a %.1f %s", minimo, maximo, unidade);
  }
}

void executarFluxo(const Cenario &c, Execucao &ex) {
  const Configuracao &cfg = c.config;
  char faixa[40];
  char erros[200];

  // Selecione a espécie da planta -> N2 Espécie cadastrada?
  imprimir(" [Ação] Selecione a espécie da planta: %s", cfg.especie);
  bool cadastrada = especieCadastrada(cfg.especie);
  imprimir(" [N2 ] Espécie cadastrada? %s", cadastrada ? "SIM" : "NÃO");
  if (!cadastrada) {
    ex.resultado = ESPECIE_NAO_ENCONTRADA;
    snprintf(ex.mensagem, sizeof(ex.mensagem),
             "A espécie \"%s\" não foi encontrada. Fluxo encerrado; a configuração não é permitida.", cfg.especie);
    return;
  }

  // Informar frequência de rega, luminosidade e temperatura
  faixaTexto(faixa, sizeof(faixa), cfg.tempMin, cfg.tempMax, "°C");
  if (cfg.intervaloRegaDias == SEM_INT) {
    imprimir(" [Ação] Informar cuidados: rega=ausente | luminosidade=%s | temperatura=%s",
             cfg.luminosidade ? cfg.luminosidade : "ausente", faixa);
  } else {
    imprimir(" [Ação] Informar cuidados: rega=a cada %d dia(s) | luminosidade=%s | temperatura=%s",
             cfg.intervaloRegaDias, cfg.luminosidade ? cfg.luminosidade : "ausente", faixa);
  }

  // Define parâmetros de monitoramento e categorias
  imprimir(" [Ação] Define parâmetros de monitoramento (temperatura=%s, ar=%s, solo=%s) e categorias (%s)",
           cfg.monitoraTemperatura ? "sim" : "não", cfg.monitoraUmidadeAr ? "sim" : "não",
           cfg.monitoraUmidadeSolo ? "sim" : "não", cfg.categoriasDefinidas ? "definidas" : "NÃO definidas");

  // Define complexidade e associa produtos e preços
  int nivel = nivelComplexidade(cfg.intervaloRegaDias, amplitude(cfg.tempMin, cfg.tempMax),
                                amplitude(cfg.arMin, cfg.arMax), amplitude(cfg.soloMin, cfg.soloMax));
  imprimir(" [Ação] Define complexidade: %s (preço mínimo R$ %.2f) | produto associado: R$ %.2f", nomeNivel(nivel),
           precoMinimo(nivel), cfg.precoProduto);

  // Valida os parâmetros -> N8 Configuração completa?
  imprimir(" [Ação] Valida os parâmetros de cuidado e monitoramento");
  bool completa = validarConfiguracao(cfg, erros, sizeof(erros));
  imprimir(" [N8 ] Configuração completa? %s", completa ? "SIM" : "NÃO");
  if (!completa) {
    ex.resultado = CONFIG_INVALIDA;
    snprintf(ex.mensagem, sizeof(ex.mensagem),
             "Campos ausentes ou parâmetros inválidos: %s. Configuração NÃO salva.", erros);
    return;
  }

  // Verifica os preços conforme complexidade -> N11 Preço compatível?
  imprimir(" [Ação] Verifica os preços conforme a complexidade");
  bool compativel = precoCompativel(nivel, cfg.precoProduto);
  imprimir(" [N11] Preço compatível? %s", compativel ? "SIM" : "NÃO");
  if (!compativel) {
    ex.resultado = PRECO_INCOMPATIVEL;
    snprintf(ex.mensagem, sizeof(ex.mensagem),
             "Divergência entre preço e complexidade: produto a R$ %.2f, mas a complexidade %s exige no mínimo "
             "R$ %.2f. Configurações NÃO salvas.",
             cfg.precoProduto, nomeNivel(nivel), precoMinimo(nivel));
    return;
  }

  // Salva as configurações (passam a valer para o monitoramento)
  configSalva = cfg;
  configSalvaValida = true;
  ex.salvou = true;
  imprimir(" [Ação] Salva as configurações");

  // Envia dados coletados da planta (dispositivo IoT publica no tópico MQTT)
  strncpy(ultimaAvaliacao.motivo, "nenhuma mensagem recebida do dispositivo", sizeof(ultimaAvaliacao.motivo) - 1);
  ultimaAvaliacao.aceita = false;
  if (c.payload != nullptr) {
    imprimir(" [Ação] Envia dados coletados da planta -> %s", TOPICO_ESTUFA);
    imprimir("        payload: %s", c.payload);
    fila.publicar(TOPICO_ESTUFA, c.payload);
  } else {
    imprimir(" [Ação] Envia dados coletados da planta -> (nenhuma mensagem enviada)");
  }

  // Valida os dados recebidos -> N16 Dados IoT válidos?
  imprimir(" [Ação] Valida os dados recebidos");
  imprimir(" [N16] Dados IoT válidos? %s", ultimaAvaliacao.aceita ? "SIM" : "NÃO");
  if (!ultimaAvaliacao.aceita) {
    ex.resultado = DADOS_IOT_INVALIDOS;
    snprintf(ex.mensagem, sizeof(ex.mensagem),
             "Falha ou ausência de dados registrada (%s). Avaliação encerrada, sem comparar nem categorizar.",
             ultimaAvaliacao.motivo);
    return;
  }

  // Compara os dados com os parâmetros configurados -> N19 Condições dentro dos limites?
  ex.comparou = true;
  imprimir(" [Ação] Compara os dados com os parâmetros configurados");
  imprimir("        medido: temperatura=%.1f °C | umidade do ar=%.1f %% | umidade do solo=%.1f %%",
           ultimaAvaliacao.temperatura, ultimaAvaliacao.umidadeAr, ultimaAvaliacao.umidadeSolo);
  bool dentro = ultimaAvaliacao.totalOcorrencias == 0;
  imprimir(" [N19] Condições dentro dos limites recomendados? %s", dentro ? "SIM" : "NÃO");

  if (dentro) {
    ex.resultado = SAUDAVEL;
    imprimir(" [Ação] Categoriza a planta como saudável");
    snprintf(ex.mensagem, sizeof(ex.mensagem), "Configurações salvas. Planta categorizada como SAUDÁVEL, sem alerta.");
    return;
  }

  // Identifica e categoriza a condição inadequada, gera alerta e notifica o administrador
  ex.resultado = ALERTA_GERADO;
  ex.alertou = true;
  ex.notificou = true;
  char detalhe[220];
  detalhe[0] = '\0';
  for (int i = 0; i < ultimaAvaliacao.totalOcorrencias; i++) {
    const Ocorrencia &o = ultimaAvaliacao.ocorrencias[i];
    char item[100];
    faixaTexto(faixa, sizeof(faixa), o.faixaMin, o.faixaMax, unidadeParametro(o.parametro));
    snprintf(item, sizeof(item), "%s %.1f %s %s (ideal %s)", nomeParametro(o.parametro), o.valorMedido,
             unidadeParametro(o.parametro), nomeCondicao(o.condicao), faixa);
    anexarErro(detalhe, sizeof(detalhe), item);
  }
  imprimir(" [Ação] Identifica e categoriza a condição inadequada: %s", detalhe);
  imprimir(" [Ação] Gera alerta de cuidado necessário");
  imprimir(" [Ação] Notifica o administrador");
  snprintf(ex.mensagem, sizeof(ex.mensagem),
           "Condição inadequada: %s. ALERTA de cuidado gerado e administrador notificado.", detalhe);
}

// Compara o obtido com o esperado da planilha
bool conferir(const Cenario &c, const Execucao &ex) {
  bool okResultado = ex.resultado == c.esperado.resultado;
  bool okSalva = ex.salvou == c.esperado.salvaConfiguracao;
  bool okComparou = ex.comparou == c.esperado.comparaParametros;
  bool okAlerta = ex.alertou == c.esperado.geraAlerta && ex.notificou == c.esperado.geraAlerta;
  imprimir(" Verificações: resultado=%s | configuração salva=%s | comparou parâmetros=%s | alerta/notificação=%s",
           okResultado ? "OK" : "FALHOU", okSalva ? "OK" : "FALHOU", okComparou ? "OK" : "FALHOU",
           okAlerta ? "OK" : "FALHOU");
  return okResultado && okSalva && okComparou && okAlerta;
}

bool executarCenario(int indice) {
  const Cenario &c = CENARIOS[indice];
  Execucao ex;
  memset(&ex, 0, sizeof(ex));
  configSalvaValida = false;  // cada teste começa sem configuração salva
  ledsApagar();

  imprimir("");
  imprimir("======================================================================");
  imprimir(" CASO DE TESTE %s | %s", c.id, c.nome);
  imprimir(" Caminho %s (%s)", c.caminho, c.nos);
  imprimir("======================================================================");
  char l1[20];
  snprintf(l1, sizeof(l1), "CT%s %s", c.id, c.caminho);
  lcdMostrar(l1, "Executando...");

  executarFluxo(c, ex);

  imprimir("----------------------------------------------------------------------");
  imprimir(" Saída esperada : %s", c.saidaEsperada);
  imprimir(" Saída obtida   : %s", ex.mensagem);
  bool ok = conferir(c, ex);
  imprimir(" >>> %s | STATUS: %s", c.id, ok ? "OK - Satisfatório" : "NOK - Insatisfatório");

  snprintf(l1, sizeof(l1), "CT%s %s %s", c.id, c.caminho, ok ? "OK" : "NOK");
  lcdMostrar(l1, rotuloLcd(ex.resultado));
  sinalizar(ex.resultado);
  return ok;
}

// Resultados guardados para o resumo geral (#01 a #06 do GFC e #07 a #11 do GE)
bool g_resGfc[TOTAL_CENARIOS];
bool g_resGe[TOTAL_CENARIOS_GE];
bool g_gfcExecutado = false;
bool g_geExecutado = false;

void executarTodos() {
  bool resultados[TOTAL_CENARIOS];
  int ok = 0;
  imprimir("");
  imprimir(">>> EXECUTANDO O ROTEIRO COMPLETO (%d casos de teste)", TOTAL_CENARIOS);
  for (int i = 0; i < TOTAL_CENARIOS; i++) {
    resultados[i] = executarCenario(i);
    g_resGfc[i] = resultados[i];
    if (resultados[i]) ok++;
    delay(PAUSA_ENTRE_TESTES_MS);
  }
  ledsApagar();
  imprimir("");
  imprimir("================== RESUMO DA EXECUÇÃO (Primeira Execução) ==================");
  for (int i = 0; i < TOTAL_CENARIOS; i++) {
    imprimir(" %s | %-3s | %s", CENARIOS[i].id, resultados[i] ? "OK" : "NOK", CENARIOS[i].nome);
  }
  imprimir("----------------------------------------------------------------------------");
  imprimir(" OK: %d | NOK: %d | NT: 0 | NA: 0 | Total: %d", ok, TOTAL_CENARIOS - ok, TOTAL_CENARIOS);
  imprimir("============================================================================");
  char l2[20];
  snprintf(l2, sizeof(l2), "OK:%d NOK:%d", ok, TOTAL_CENARIOS - ok);
  g_gfcExecutado = true;
  lcdMostrar("RESUMO TESTES", l2);
}

// ---------------------------------------------------------------------
// 7b. EXECUÇÃO DAS SEQUÊNCIAS DO GRAFO DE ESTADOS (S1 a S5)
//     O caminho é montado a partir do que o sistema realmente fez e é
//     comparado com a sequência de transições esperada do Quadro 2b.
// ---------------------------------------------------------------------
const char* nomeEstado(EstadoLeitura e) {
  switch (e) {
    case LEITURA_REJEITADA: return "Rejeitada";
    case LEITURA_SEM_MONITORAMENTO: return "Sem monitoramento";
    case LEITURA_CONDICAO_IDEAL: return "Condição ideal";
    default: return "Fora do limite";
  }
}

const char* rotuloLcdEstado(EstadoLeitura e) {
  switch (e) {
    case LEITURA_REJEITADA: return "REJEITADA";
    case LEITURA_SEM_MONITORAMENTO: return "SEM MONITORAM.";
    case LEITURA_CONDICAO_IDEAL: return "CONDICAO IDEAL";
    default: return "FORA DO LIMITE";
  }
}

void acrescentar(char* destino, size_t tamanho, const char* texto) {
  strncat(destino, texto, tamanho - strlen(destino) - 1);
}

bool executarSequencia(int indice) {
  const CenarioGE &c = CENARIOS_GE[indice];
  ledsApagar();
  configSalva = CENARIOS[0].config;     // espécie Violeta Africana
  configSalvaValida = c.alertaAtivo;    // existe (ou não) alerta ativo para o dispositivo

  imprimir("");
  imprimir("======================================================================");
  imprimir(" CASO DE TESTE %s | Sequência %s do GE | %s", c.id, c.sequencia, c.nome);
  imprimir(" Sequência esperada: %s", c.transicoes);
  imprimir("======================================================================");
  char l1[20];
  snprintf(l1, sizeof(l1), "CT%s %s", c.id, c.sequencia);
  lcdMostrar(l1, "Executando...");

  imprimir(" [T0 ] Mensagem MQTT recebida em %s", c.topico);
  imprimir("        payload: %s", c.payload);

  strncpy(ultimaAvaliacao.motivo, "nenhuma mensagem recebida", sizeof(ultimaAvaliacao.motivo) - 1);
  ultimaAvaliacao.aceita = false;
  ultimaAvaliacao.avaliada = false;
  ultimaAvaliacao.totalOcorrencias = 0;
  receberComSeguranca(c.topico, c.payload);  // mesmo tratamento que o assinante da fila faz

  const DispositivoIot* dispositivo = buscarDispositivo(c.topico);
  bool dispositivoOk = dispositivo != nullptr && dispositivo->ativo;

  char seq[72];
  snprintf(seq, sizeof(seq), "T0");
  EstadoLeitura estado;
  const char* aresta;

  imprimir(" [Estado] Validando dispositivo");
  imprimir(" [C1 ] Dispositivo cadastrado e ativo? %s", dispositivoOk ? "SIM" : "NÃO");
  if (!dispositivoOk) {
    acrescentar(seq, sizeof(seq), " → T1");
    estado = LEITURA_REJEITADA;
    aresta = "F1";
    imprimir("        motivo: %s", ultimaAvaliacao.motivo);
  } else {
    acrescentar(seq, sizeof(seq), " → T2");
    imprimir(" [Estado] Validando dados");
    imprimir(" [C2 ] Payload, umidades e calibração válidos? %s", ultimaAvaliacao.aceita ? "SIM" : "NÃO");
    if (!ultimaAvaliacao.aceita) {
      acrescentar(seq, sizeof(seq), " → T3");
      estado = LEITURA_REJEITADA;
      aresta = "F1";
      imprimir("        motivo: %s", ultimaAvaliacao.motivo);
    } else {
      acrescentar(seq, sizeof(seq), " → T4");
      imprimir(" [Estado] Armazenada: temperatura=%.2f °C | umidade do ar=%.1f %% | umidade do solo=%.1f %%",
               ultimaAvaliacao.temperatura, ultimaAvaliacao.umidadeAr, ultimaAvaliacao.umidadeSolo);
      imprimir(" [C3 ] Há alerta ativo com espécie ativa? %s", ultimaAvaliacao.avaliada ? "SIM" : "NÃO");
      if (!ultimaAvaliacao.avaliada) {
        acrescentar(seq, sizeof(seq), " → T5");
        estado = LEITURA_SEM_MONITORAMENTO;
        aresta = "F2";
      } else {
        acrescentar(seq, sizeof(seq), " → T6");
        imprimir(" [Estado] Em avaliação (faixa ideal da espécie: temperatura 20 a 24 °C)");
        bool fora = ultimaAvaliacao.totalOcorrencias > 0;
        imprimir(" [C4 ] Algum parâmetro fora da faixa? %s", fora ? "SIM" : "NÃO");
        if (!fora) {
          acrescentar(seq, sizeof(seq), " → T7");
          estado = LEITURA_CONDICAO_IDEAL;
          aresta = "F3";
        } else {
          acrescentar(seq, sizeof(seq), " → T8");
          estado = LEITURA_FORA_DO_LIMITE;
          aresta = "F4";
          for (int i = 0; i < ultimaAvaliacao.totalOcorrencias; i++) {
            const Ocorrencia &o = ultimaAvaliacao.ocorrencias[i];
            imprimir("        ocorrência: %s = %.2f %s -> %s", nomeParametro(o.parametro), o.valorMedido,
                     unidadeParametro(o.parametro), nomeCondicao(o.condicao));
          }
        }
      }
    }
  }
  acrescentar(seq, sizeof(seq), " → ");
  acrescentar(seq, sizeof(seq), aresta);

  bool okEstado = estado == c.esperado;
  bool okSequencia = strcmp(seq, c.transicoes) == 0;
  bool ok = okEstado && okSequencia;

  imprimir("----------------------------------------------------------------------");
  imprimir(" Saída esperada     : %s", c.saidaEsperada);
  imprimir(" Estado final       : %s (esperado: %s)", nomeEstado(estado), nomeEstado(c.esperado));
  imprimir(" Sequência obtida   : %s", seq);
  imprimir(" Sequência esperada : %s", c.transicoes);
  imprimir(" Verificações: estado final=%s | sequência de transições=%s", okEstado ? "OK" : "FALHOU",
           okSequencia ? "OK" : "FALHOU");
  imprimir(" >>> %s (%s) | STATUS: %s", c.id, c.sequencia, ok ? "OK - Satisfatório" : "NOK - Insatisfatório");

  snprintf(l1, sizeof(l1), "CT%s %s %s", c.id, c.sequencia, ok ? "OK" : "NOK");
  lcdMostrar(l1, rotuloLcdEstado(estado));
  sinalizar(estado == LEITURA_CONDICAO_IDEAL ? SAUDAVEL
                                              : (estado == LEITURA_FORA_DO_LIMITE ? ALERTA_GERADO : DADOS_IOT_INVALIDOS));
  return ok;
}

void executarTodasSequencias() {
  bool resultados[TOTAL_CENARIOS_GE];
  int ok = 0;
  imprimir("");
  imprimir(">>> EXECUTANDO AS SEQUÊNCIAS INDEPENDENTES DO GE (%d casos de teste)", TOTAL_CENARIOS_GE);
  for (int i = 0; i < TOTAL_CENARIOS_GE; i++) {
    resultados[i] = executarSequencia(i);
    g_resGe[i] = resultados[i];
    if (resultados[i]) ok++;
    delay(PAUSA_ENTRE_TESTES_MS);
  }
  ledsApagar();
  imprimir("");
  imprimir("============ RESUMO DA EXECUÇÃO - SEQUÊNCIAS DO GE (Primeira Execução) ============");
  for (int i = 0; i < TOTAL_CENARIOS_GE; i++) {
    imprimir(" %s | %s | %-3s | %s", CENARIOS_GE[i].id, CENARIOS_GE[i].sequencia, resultados[i] ? "OK" : "NOK",
             CENARIOS_GE[i].nome);
  }
  imprimir("--------------------------------------------------------------------------------");
  imprimir(" OK: %d | NOK: %d | NT: 0 | NA: 0 | Total: %d", ok, TOTAL_CENARIOS_GE - ok, TOTAL_CENARIOS_GE);
  imprimir("================================================================================");
  char l2[20];
  snprintf(l2, sizeof(l2), "GE OK:%d NOK:%d", ok, TOTAL_CENARIOS_GE - ok);
  g_geExecutado = true;
  lcdMostrar("RESUMO SEQ. GE", l2);
}

// Resumo único com os 11 casos de teste: cabe numa tela do Serial Monitor
void resumoGeral() {
  int ok = 0;
  int nok = 0;
  int nt = 0;
  imprimir("");
  imprimir("========================= RESUMO GERAL DOS TESTES =========================");
  for (int i = 0; i < TOTAL_CENARIOS; i++) {
    const char* status = !g_gfcExecutado ? "NT" : (g_resGfc[i] ? "OK" : "NOK");
    if (!g_gfcExecutado) nt++; else if (g_resGfc[i]) ok++; else nok++;
    imprimir(" %s | %-3s | GFC %s | %s", CENARIOS[i].id, status, CENARIOS[i].caminho, CENARIOS[i].nome);
  }
  for (int i = 0; i < TOTAL_CENARIOS_GE; i++) {
    const char* status = !g_geExecutado ? "NT" : (g_resGe[i] ? "OK" : "NOK");
    if (!g_geExecutado) nt++; else if (g_resGe[i]) ok++; else nok++;
    imprimir(" %s | %-3s | GE  %s | %s", CENARIOS_GE[i].id, status, CENARIOS_GE[i].sequencia,
             CENARIOS_GE[i].nome);
  }
  imprimir("---------------------------------------------------------------------------");
  imprimir(" OK: %d | NOK: %d | NT: %d | NA: 0 | Total: %d", ok, nok, nt, ok + nok + nt);
  imprimir("===========================================================================");
  char l2[20];
  snprintf(l2, sizeof(l2), "OK:%d NOK:%d NT:%d", ok, nok, nt);
  lcdMostrar("RESUMO GERAL", l2);
}

// ---------------------------------------------------------------------
// 8. MODO SENSORES (DHT11 + potenciômetro) E MENU
// ---------------------------------------------------------------------
bool modoSensores = false;
unsigned long ultimaLeitura = 0;

void lerSensoresEProcessar() {
  TempAndHumidity leitura = dht.getTempAndHumidity();
  int bruto = analogRead(PIN_SOLO);

  // Verifica se o DHT11 conseguiu fornecer uma leitura válida antes de publicar
  if (!isfinite(leitura.temperature) || !isfinite(leitura.humidity) ||
      leitura.humidity < 0 || leitura.humidity > 100) {
    imprimir("");
    imprimir("[ESP32] ERRO: Não foi possível ler o DHT11.");
    imprimir("        Verifique a alimentação, o GND e o fio DATA no GPIO %d.", PIN_DHT);
    imprimir("        Nenhum payload foi enviado para evitar dados inválidos.");

    lcdMostrar("ERRO DHT11", "Verifique sensor");
    ledsApagar();
    digitalWrite(PIN_LED_AMARELO, HIGH);
    return;
  }

  char payload[TAMANHO_PAYLOAD];
  snprintf(payload, sizeof(payload), "{\"temperatura\":%.1f,\"umidadeAr\":%.1f,\"umidadeSoloRaw\":%d}",
           leitura.temperature, leitura.humidity, bruto);
  imprimir("");
  imprimir("[ESP32] Leitura dos sensores:");
  imprimir("        %s", payload);

  receberComSeguranca(TOPICO_ESTUFA, payload);
  if (!ultimaAvaliacao.aceita) {
    lcdMostrar("LEITURA INVALIDA", "ver Serial");
    ledsApagar();
    digitalWrite(PIN_LED_AMARELO, HIGH);
    return;
  }

  publicarLeituraReal(payload);
  imprimir("[ESP32] Leitura aceita: temperatura=%.1f °C | umidade do ar=%.1f %% | umidade do solo=%.1f %%",
           ultimaAvaliacao.temperatura, ultimaAvaliacao.umidadeAr, ultimaAvaliacao.umidadeSolo);

  char l1[20];
  char l2[20];
  snprintf(l1, sizeof(l1), "T%.0f A%.0f S%.0f%%", ultimaAvaliacao.temperatura, ultimaAvaliacao.umidadeAr,
           ultimaAvaliacao.umidadeSolo);
  ledsApagar();
  if (ultimaAvaliacao.totalOcorrencias == 0) {
    imprimir("[ESP32] Todos os parâmetros dentro da faixa: planta SAUDÁVEL");
    snprintf(l2, sizeof(l2), "SAUDAVEL");
    digitalWrite(PIN_LED_VERDE, HIGH);
  } else {
    for (int i = 0; i < ultimaAvaliacao.totalOcorrencias; i++) {
      const Ocorrencia &o = ultimaAvaliacao.ocorrencias[i];
      imprimir("[ESP32] ALERTA: %s = %.1f %s -> %s", nomeParametro(o.parametro), o.valorMedido,
               unidadeParametro(o.parametro), nomeCondicao(o.condicao));
    }
    snprintf(l2, sizeof(l2), "ALERTA (%d)", ultimaAvaliacao.totalOcorrencias);
    digitalWrite(PIN_LED_VERMELHO, HIGH);
    bipe(880, 200);
  }
  lcdMostrar(l1, l2);
}

void mostrarMenu() {
  imprimir("");
  imprimir("================================ MENU ================================");
  imprimir("  1 a 6 : executar um caso de teste (#01 a #06)   t : executar todos os 6");
  imprimir("  a a e : executar uma sequência do GE (S1 a S5)  g : executar todas as 5 sequências");
  imprimir("  r     : resumo geral dos 11 testes");
  imprimir("  l     : modo sensores (DHT11 + potenciômetro); qualquer tecla sai");
  imprimir("  m     : mostrar este menu");
  imprimir("======================================================================");
}

void iniciarModoSensores() {
  configSalva = CENARIOS[0].config;  // espécie Violeta Africana já configurada
  configSalvaValida = true;
  modoSensores = true;
  ultimaLeitura = millis() - INTERVALO_SENSORES_MS;
  imprimir("");
  imprimir(">>> MODO SENSORES: configuração da %s ativa.", configSalva.especie);
  imprimir("    Lendo o DHT11 e a entrada analógica de umidade do solo a cada 3 s.");
  imprimir("    Faixa ideal: temperatura 20-24 °C | ar 60-70 %% | solo 50-60 %%. Envie qualquer tecla para sair.");
}

void tratarComando(char comando) {
  if (comando >= '1' && comando <= '6') {
    executarCenario(comando - '1');
    mostrarMenu();
  } else if (comando == 't' || comando == 'T') {
    executarTodos();
    mostrarMenu();
  } else if (comando >= 'a' && comando <= 'e') {
    executarSequencia(comando - 'a');
    mostrarMenu();
  } else if (comando == 'g' || comando == 'G') {
    executarTodasSequencias();
    mostrarMenu();
  } else if (comando == 'r' || comando == 'R') {
    resumoGeral();
    mostrarMenu();
  } else if (comando == 'l' || comando == 'L') {
    iniciarModoSensores();
  } else if (comando == 'm' || comando == 'M' || comando == '?') {
    mostrarMenu();
  } else {
    imprimir("Comando desconhecido: '%c'. Digite m para ver o menu.", comando);
  }
}

// ---------------------------------------------------------------------
// 9. SETUP E LOOP
// ---------------------------------------------------------------------
void setup() {
  Serial.begin(115200);

  pinMode(PIN_LED_VERDE, OUTPUT);
  pinMode(PIN_LED_AMARELO, OUTPUT);
  pinMode(PIN_LED_VERMELHO, OUTPUT);
  pinMode(PIN_BUZZER, OUTPUT);
  pinMode(PIN_SOLO, INPUT);
  ledsApagar();

  dht.setup(PIN_DHT, DHTesp::DHT11);

#if USAR_LCD
  Wire.begin(SDA_PIN, SCL_PIN);

  if (!display.begin(SSD1306_SWITCHCAPVCC, 0x3C)) {
    Serial.println("Erro ao iniciar OLED!");
    while (true) {
      delay(100);
    }
  }

  display.clearDisplay();
  display.setTextColor(SSD1306_WHITE);
  display.setTextSize(1);
  display.display();
#endif

  imprimir("");
  imprimir("======================================================================");
  imprimir("  GREENHOUSE - Monitoramento IoT (ESP32)  |  Grupo Lovelace");
  imprimir("  UC: Configurar cuidados e monitoramento de plantas (RF15/RF16/RF17)");
  imprimir("======================================================================");
  lcdMostrar("GREENHOUSE IoT", "Iniciando...");

  int assinados = iniciarMonitoramento();
  imprimir("[ESP32] Monitoramento local iniciado: %d dispositivo(s) em %s", assinados, TOPICO_ESTUFA);
  mostrarMenu();
  iniciarTelemetria();
  iniciarModoSensores();
}

void loop() {
  if (Serial.available() > 0) {
    char c = (char)Serial.read();
    if (c == '\n' || c == '\r' || c == ' ') return;
    if (modoSensores) {
      modoSensores = false;
      ledsApagar();
      imprimir(">>> Modo sensores encerrado.");
      mostrarMenu();
      return;
    }
    tratarComando(c);
  }

  if (modoSensores && millis() - ultimaLeitura >= INTERVALO_SENSORES_MS) {
    ultimaLeitura = millis();
    lerSensoresEProcessar();
  }
}