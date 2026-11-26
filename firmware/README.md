# ESP32 da estufa 1

O ESP32 inicia o monitoramento ao ligar. A cada três segundos, lê o DHT11 e a entrada analógica de solo, atualiza o OLED e os indicadores locais e, quando conectado, envia a leitura para `greenhouse/estufa1/leitura`. O backend Kotlin recebe a mensagem e registra a hora de recebimento.

## Configurar e gravar a placa

1. Na Arduino IDE, adicione `https://espressif.github.io/arduino-esp32/package_esp32_index.json` às URLs do Gerenciador de Placas. Instale **esp32 by Espressif Systems 2.0.17**. O firmware usa a API ESP-MQTT do ESP-IDF 4.4 incluída nessa versão.
2. Pelo Gerenciador de Bibliotecas, instale **DHT sensor library for ESPx** (1.19.0), **Adafruit SSD1306** (2.5.17), **Adafruit GFX Library** (2.0.0) e sua dependência **Adafruit BusIO** (1.17.4).
3. Abra `sketch_greenhouse/sketch_greenhouse.ino`. Nesta cópia de trabalho, `sketch_greenhouse/secrets.h` já contém a configuração MQTT do `.env` local. Preencha `WIFI_SSID` e `WIFI_PASSWORD` para a rede de 2,4 GHz da placa.
4. Em um novo checkout, copie `secrets.example.h` para `secrets.h` na pasta do sketch. Preencha `MQTT_HOST` com apenas o hostname de `MQTT_BROKER_URL` e copie `MQTT_USERNAME` e `MQTT_PASSWORD` do ambiente da equipe. A porta TLS é 8883. Repita a gravação quando alterar as credenciais.
5. Selecione **ESP32 Dev Module** para o ESP32 clássico com a pinagem abaixo, escolha a porta USB da placa e clique em **Verificar** e **Carregar**. Abra o Monitor Serial a **115200 baud** para acompanhar a conexão e os recibos `PUBACK`.

| Componente | Conexão |
| --- | --- |
| DHT11 | GPIO 15 |
| Sensor de solo, saída analógica | GPIO 34 |
| OLED SSD1306, 128 × 64, endereço I²C 0x3C | SDA 21, SCL 22 |
| LEDs verde, amarelo e vermelho | GPIO 26, 27 e 14 |
| Buzzer | GPIO 25 |

## Publicação e operação offline

Neste exemplo, o payload contém duas medidas decimais e o valor bruto de 2100 lido pelo ADC.

```json
{"temperatura":22.0,"umidadeAr":65.0,"umidadeSoloRaw":2100}
```

O publicador usa **QoS 1** e **retain desativado**. A assinatura do backend também solicita QoS 1. Como o PubSubClient publica somente em QoS 0, foi escolhido o ESP-MQTT do core. O evento `PUBACK` confirma a entrega ao broker. QoS 1 permite reentrega de uma mensagem se a confirmação se perder. [ESP-MQTT](https://docs.espressif.com/projects/esp-idf/en/v4.4.7/esp32/api-reference/protocols/mqtt.html) · [PubSubClient](https://github.com/knolleary/pubsubclient)

A tarefa de rede solicita o horário via SNTP e conecta por hostname com a raiz ISRG Root X1. O firmware complementa a verificação do mbedTLS desse core com a conferência das datas de cada certificado. O ID começa com `greenhouse-esp32-estufa1-` e termina com o identificador do chip.

Os sensores e os indicadores locais continuam a cada ciclo durante falhas de Wi-Fi, DNS, horário ou broker. As novas tentativas de conexão usam intervalo de 15 s. Amostras coletadas offline ficam apenas no monitoramento local. O cliente aceita uma publicação por vez aguardando confirmação. Quando a conexão volta, as próximas leituras seguem normalmente. Leituras rejeitadas pelo processamento local ficam fora do MQTT.

## Iniciar o backend

Na raiz do projeto, use o `.env` da equipe com o banco de destino, JWT e as configurações MQTT existentes. Confira `MQTT_CLIENT_ID=greenhouse-server-anahi`, `MQTT_QOS=1` e a URL no formato `ssl://<hostname>:8883`. Inicie o backend em Bash.

```bash
SPRING_CONFIG_IMPORT="file:$PWD/.env[.properties]" ./gradlew :serverApp:bootRun
```

Na inicialização, o Flyway aplica as migrações pendentes ao banco desse ambiente. A V21 cadastra o dispositivo ativo da estufa 1 com calibração inicial de 3200 (seco) e 1200 (úmido), conforme o sketch. Um cadastro anterior para esse tópico conserva sua calibração e seu estado. Confira os extremos com o sensor instalado. O backend consulta os dispositivos ativos no início da aplicação e registra `Assinado o tópico MQTT greenhouse/estufa1/leitura` quando a assinatura é aceita.

## Acompanhar uma leitura física

1. No painel do cluster HiveMQ Cloud, abra **Web Client** e conecte-se com uma credencial que possa assinar o tópico. Se o cliente pedir endereço, use o endpoint **WebSocket seguro** mostrado no painel. Use um ID distinto dos IDs do backend e da placa. [Guia do HiveMQ Cloud](https://docs.hivemq.com/hivemq-cloud/quick-start-guide.html)
2. Assine `greenhouse/estufa1/leitura` com QoS 1 e aguarde a próxima coleta. O Monitor Serial mostra o ID da publicação e depois o `PUBACK`. O Web Client mostra o JSON. Para testes automatizados, use mocks ou um tópico exclusivo sob `greenhouse/teste/`.
3. Consulte o PostgreSQL do backend para confirmar a persistência.

```sql
SELECT l.data_leitura, l.temperatura, l.umidade_ar, l.umidade_solo_raw, l.umidade_solo
FROM leitura_iot l
JOIN dispositivo_iot d ON d.id = l.dispositivo_iot_id
WHERE d.topico_mqtt = 'greenhouse/estufa1/leitura'
ORDER BY l.data_leitura DESC, l.id DESC
LIMIT 10;
```

`LeituraIotService` valida a mensagem, armazena o valor bruto e calcula o percentual com a calibração do cadastro. Com os valores iniciais, ADC 2100 corresponde a 55%. O serviço compara a leitura aos limites das espécies ativas com alerta ativo para aquele dispositivo. O envio de e-mail depende da configuração SMTP e de destinatário no backend. A configuração local da Violeta usada pelo display é independente dos alertas cadastrados no banco.

## Executar os testes locais

No Monitor Serial, envie uma tecla como `m` para sair do monitoramento e abrir o menu. Em seguida, envie `t` para os seis casos GFC, `g` para as cinco sequências GE e `r` para o resumo dos onze. Use `1` a `6` e `a` a `e` para casos individuais, e `l` para retomar a coleta física. Os payloads dos testes percorrem exclusivamente o processamento local.

Para compilar o firmware pela CLI, com o core, bibliotecas e `secrets.h` preparados, e executar a suíte do backend, execute os comandos na raiz do projeto.

```bash
arduino-cli compile --fqbn esp32:esp32:esp32 firmware/sketch_greenhouse
./gradlew :serverApp:test :serverApp:bootJar
```

## Diagnóstico

| Sintoma | O que conferir |
| --- | --- |
| Erro de DHT11 | Alimentação, GND, fio de dados no GPIO 15 e modelo do sensor. |
| Apenas leituras locais | SSID e senha em `secrets.h`, rede 2,4 GHz e alcance. Verifique também se o modo sensores está ativo. |
| Wi-Fi conectado e MQTT sem conexão | Acesso a DNS e NTP (UDP 123), saída TCP 8883 e hostname do cluster. Se a CA mudar, valide a nova cadeia e atualize `ca_hivemq.h`. |
| Erro de autenticação ou desconexão ao publicar | Credenciais locais, status do cluster, permissão de publicação no tópico exato e IDs de cliente distintos. |
| Web Client recebe, mas o banco fica sem leitura | Procure o log de assinatura e eventuais descartes. Confira se o backend usa o mesmo cluster e o banco esperado, se o dispositivo está ativo e se os dois valores de calibração são distintos. Reinicie o backend após ativar um dispositivo. |
| Leitura persistida sem e-mail | Confira os limites da espécie, os parâmetros monitorados pelo alerta, o SMTP e os logs de notificação. |
