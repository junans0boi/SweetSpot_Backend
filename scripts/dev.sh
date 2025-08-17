# scripts/dev.sh
#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."

# .env를 스프링이 자동 로드하도록 application.properties에서 import 했다는 가정.
# 프로필 기본값
export SPRING_PROFILES_ACTIVE="${SPRING_PROFILES_ACTIVE:-dev}"

# 가장 빠른 개발 루프: 테스트 스킵 + bootRun
./gradlew bootRun -x test --no-rebuild \
  -Dspring.output.ansi.enabled=always \
  --args="--spring.profiles.active=${SPRING_PROFILES_ACTIVE}"