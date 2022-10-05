%dw 2.0
output application/json
var payloadResponse = if (isEmpty(payload) or payload=="") {} else payload
var test = payload.^mediaType
---
{
  "x-event-code": p('x-event.success.get.code'), 
  "x-event-msg": p('x-event.success.get.message'), 
  "data": payload default {}
}