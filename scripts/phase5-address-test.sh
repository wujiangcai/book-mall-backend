#!/usr/bin/env bash
set -euo pipefail

BASE_URL="${BASE_URL:-http://localhost:8003}"
DB_HOST="${DB_HOST:-localhost}"
DB_PORT="${DB_PORT:-3306}"
DB_NAME="${DB_NAME:-book_mall}"
DB_USER="${DB_USER:-root}"
DB_PASS="${DB_PASS:-}"
MYSQL_BIN="${MYSQL_BIN:-mysql}"

TS=$(date +%s)
USERNAME="addr${TS}"
PASSWORD="test123"
PHONE="1390013$(printf '%04d' $((TS % 10000)))"

run_sql() {
  if [ -n "$DB_PASS" ]; then
    "$MYSQL_BIN" -h "$DB_HOST" -P "$DB_PORT" -u "$DB_USER" -p"$DB_PASS" "$DB_NAME" -e "$1"
  else
    "$MYSQL_BIN" -h "$DB_HOST" -P "$DB_PORT" -u "$DB_USER" "$DB_NAME" -e "$1"
  fi
}

if ! command -v "$MYSQL_BIN" >/dev/null 2>&1; then
  echo "mysql client not found. Set MYSQL_BIN or install mysql." >&2
  exit 1
fi

if ! command -v curl >/dev/null 2>&1; then
  echo "curl not found." >&2
  exit 1
fi

if ! command -v python >/dev/null 2>&1 && ! command -v python3 >/dev/null 2>&1; then
  echo "python not found." >&2
  exit 1
fi

PYTHON_BIN=python
if command -v python3 >/dev/null 2>&1; then
  PYTHON_BIN=python3
fi

register_resp=$(curl -sS -X POST "$BASE_URL/api/front/auth/register" \
  -H "Content-Type: application/json" \
  -d "{\"username\":\"$USERNAME\",\"password\":\"$PASSWORD\",\"nickname\":\"addr_test\",\"phone\":\"$PHONE\",\"email\":\"${USERNAME}@example.com\"}")

register_code=$(REGISTER_RESP="$register_resp" $PYTHON_BIN -c "import json,os; raw=os.environ.get('REGISTER_RESP',''); print(json.loads(raw).get('code','') if raw else '')")

if [ "$register_code" != "200" ]; then
  echo "Register failed: $register_resp" >&2
  exit 1
fi

token=$(REGISTER_RESP="$register_resp" $PYTHON_BIN -c "import json,os; raw=os.environ.get('REGISTER_RESP',''); print(json.loads(raw).get('data',{}).get('token','') if raw else '')")

if [ -z "$token" ]; then
  echo "Register failed: $register_resp" >&2
  exit 1
fi

list_addresses() {
  curl -sS -X GET "$BASE_URL/api/front/user/addresses" -H "Authorization: $token"
}

create_addr() {
  curl -sS -X POST "$BASE_URL/api/front/user/addresses" \
    -H "Authorization: $token" \
    -H "Content-Type: application/json" \
    -d "$1"
}

set_default() {
  curl -sS -X PUT "$BASE_URL/api/front/user/addresses/$1/default" \
    -H "Authorization: $token"
}

delete_addr() {
  curl -sS -X DELETE "$BASE_URL/api/front/user/addresses/$1" \
    -H "Authorization: $token"
}

extract_id() {
  $PYTHON_BIN -c "import json,sys; obj=json.loads(sys.stdin.read()); arr=obj.get('data') or []; print(arr[0].get('id') if arr else '')"
}

extract_id_by_index() {
  $PYTHON_BIN -c "import json,sys; idx=int(sys.argv[1]); obj=json.loads(sys.stdin.read()); arr=obj.get('data') or []; print(arr[idx].get('id') if len(arr)>idx else '')" "$1"
}

extract_default_id() {
  $PYTHON_BIN -c "import json,sys; obj=json.loads(sys.stdin.read()); arr=obj.get('data') or []; print(next((i.get('id') for i in arr if i.get('isDefault')==1), ''))"
}

assert_default_id() {
  expected="$1"
  actual="$2"
  if [ "$expected" != "$actual" ]; then
    echo "Expected default id $expected, got $actual" >&2
    exit 1
  fi
}

addr1_payload='{"receiverName":"User1","phone":"13800138000","province":"Guangdong","city":"Shenzhen","district":"Nanshan","detailAddress":"Tech Park 1","isDefault":0}'
create_addr "$addr1_payload" >/dev/null
list1=$(list_addresses)
addr1_id=$(extract_id <<<"$list1")
default1=$(extract_default_id <<<"$list1")
if [ -z "$addr1_id" ]; then
  echo "Failed to create/list first address: $list1" >&2
  exit 1
fi
assert_default_id "$addr1_id" "$default1"

aaddr2_payload='{"receiverName":"User2","phone":"13900139000","province":"Guangdong","city":"Shenzhen","district":"Futian","detailAddress":"Downtown 2","isDefault":1}'
create_addr "$aaddr2_payload" >/dev/null
list2=$(list_addresses)
addr2_id=$(extract_id_by_index 0 <<<"$list2")
default2=$(extract_default_id <<<"$list2")
if [ -z "$addr2_id" ]; then
  echo "Failed to create/list second address: $list2" >&2
  exit 1
fi
assert_default_id "$addr2_id" "$default2"

set_default "$addr1_id" >/dev/null
list3=$(list_addresses)
default3=$(extract_default_id <<<"$list3")
assert_default_id "$addr1_id" "$default3"

delete_addr "$addr2_id" >/dev/null
list4=$(list_addresses)
remaining_default=$(extract_default_id <<<"$list4")
assert_default_id "$addr1_id" "$remaining_default"

user_id=$(run_sql "SELECT id FROM user WHERE username='$USERNAME'" | tail -n 1)

if [ -z "$user_id" ]; then
  echo "Failed to resolve user id for $USERNAME" >&2
  exit 1
fi

run_sql "INSERT INTO \`order\` (order_no, user_id, total_amount, status, address_id) VALUES ('TEST${TS}', ${user_id}, 99.99, 2, ${addr1_id});"

resp=$(delete_addr "$addr1_id")
code=$(RESP="$resp" $PYTHON_BIN -c "import json,os; raw=os.environ.get('RESP',''); print(json.loads(raw).get('code','') if raw else '')")

if [ "$code" != "400" ]; then
  echo "Expected 400 when deleting address with pending shipment order, got: $resp" >&2
  exit 1
fi

echo "All Phase 5 address tests passed."