#!/usr/bin/env bash
# Gera trafego de login para popular o dashboard do Grafana.
# Uso:  ./scripts/gerar-trafego.sh            (Linux / macOS / Git Bash no Windows)
#       BASE=http://localhost:8080 ./scripts/gerar-trafego.sh

BASE="${BASE:-http://localhost:8080}"

json() {
  echo "{\"username\":\"$1\",\"senha\":\"$2\"}"
}

login() {
  curl -s -o /dev/null -w "  login $1 ($2) -> HTTP %{http_code}\n" \
    -X POST "$BASE/login" -H "Content-Type: application/json" -d "$(json "$1" "$2")"
}

echo "== 1) Logins validos (usuario / user123) =="
for i in 1 2 3 4 5 6; do login usuario user123; done

echo "== 2) Logins invalidos (usuario inexistente) =="
for i in 1 2 3; do login fantasma qualquer; done

echo "== 3) Criando usuarios de teste (admin da manutencao: manutencao / manut123) =="
for u in aluno1 aluno2 aluno3; do
  curl -s -o /dev/null -w "  criar $u -> HTTP %{http_code}\n" \
    -u manutencao:manut123 -X POST "$BASE/" -H "Content-Type: application/json" -d "$(json "$u" abc123)"
done

echo "== 4) Bloqueando aluno1 e aluno2 (2 falhas seguidas cada) =="
for u in aluno1 aluno2; do
  login "$u" errada
  login "$u" errada
done

echo "== 5) aluno3: 1 falha seguida de login valido (NAO deve bloquear) =="
login aluno3 errada
login aluno3 abc123

echo "== 6) Tentativa com senha correta em usuario bloqueado (deve dar 401) =="
login aluno1 abc123

echo "== 7) Admin desbloqueia aluno2 (admin / admin123) =="
curl -s -o /dev/null -w "  unlock aluno2 -> HTTP %{http_code}\n" \
  -u admin:admin123 -X PUT "$BASE/unlock/aluno2"

echo
echo "Pronto! Esperado no dashboard: usuarios bloqueados = 1 (aluno1)."
echo "Aguarde ~10s (scrape do Prometheus) e abra http://localhost:3000"
