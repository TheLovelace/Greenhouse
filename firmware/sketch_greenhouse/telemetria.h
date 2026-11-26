#pragma once

constexpr char TOPICO_ESTUFA[] = "greenhouse/estufa1/leitura";
constexpr unsigned long INTERVALO_SENSORES_MS = 3000;
constexpr unsigned TAMANHO_PAYLOAD = 120;

void iniciarTelemetria();
void publicarLeituraReal(const char (&payload)[TAMANHO_PAYLOAD]);
