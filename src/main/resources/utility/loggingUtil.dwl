%dw 2.0

var dateFormat = Mule::p('app.log.datetime.format')
fun buildInfoLog(messageId, flow, direction, destination, message, identifier, responseCode, attributes, payload) =
	buildLog("INFO", "SUCCESS", messageId, flow, direction, destination, message, identifier, responseCode, attributes, payload)

fun buildDebugLog(messageId, flow, direction, destination, message, identifier, responseCode, attributes, payload) =
	buildLog("DEBUG", "SUCCESS", messageId, flow, direction, destination, message, identifier, responseCode, attributes, payload)

fun buildTraceLog(messageId, flow, direction, destination, message, identifier, responseCode, attributes, payload) =
	buildLog("TRACE", "SUCCESS", messageId, flow, direction, destination, message, identifier, responseCode, attributes, payload)

fun buildErrorLog(messageId, flow, direction, destination, message, identifier, responseCode, attributes, payload) =
	builErrorTraceLog("ERROR", "FAILURE", messageId, flow, direction, destination, message, identifier, responseCode, attributes, payload)

fun buildCriticalLog(messageId, flow, direction, destination,  message, identifier, responseCode, attributes, payload) =
	buildLog("CRITICAL", "FAILURE", messageId, flow, direction, destination, message, identifier, responseCode, attributes, payload)

fun createRequestAttributes(requestPath, requestMethod, queryString, destination)=
{
	requestPath: requestPath,
	requestMethod: requestMethod,
	queryString: queryString,
	destination: destination
}

fun logTrim(str)=
if (sizeOf(str) > 250) str[0 to 250] ++ "..." else str

fun buildLog(level, result, messageId, flow, direction, destination, message, identifier, responseCode, attributes, payload) =

{
    "indexName": Mule::p('log.stream.name'),
    "auditEntry": {
        "instance": Mule::p('app.log.instance'),
        "id": messageId,
        "flow": flow,
        "identifier": identifier,
        "requestBody": if (direction == "Request" and level == "DEBUG" ) write(payload, "application/json") else  "",
        "responseBody": if (direction == "Response" and level == "DEBUG" ) write(payload, "application/json") else  "",
        "requestPath": if (direction == "Request") (attributes.requestPath default "") else "",
        "requestMethod": if (direction == "Request") (attributes.requestMethod default "") else "",
		"queryString": logTrim(if (direction == "Request") (attributes.queryString default "") else ""),
		"responseCode": if (direction == "Response") (responseCode default "") else "",
		"environment": Mule::p('env'),
		"destination": if (direction == "Request") (attributes.destination default "") else "",
		"source": Mule::p('app.name'),
		"result": result,
		"message": logTrim((message ++ (if (!(["Request", "Response"] contains direction) and !isEmpty(payload)) write(payload, "application/json") else ""))),
		"level": level,
        "direction": direction,
        "timestamp": now() as String {format: dateFormat}
        }
}


fun builErrorTraceLog(level, result, messageId, flow, direction, destination, message, identifier, responseCode, attributes, payload) =
{
    "indexName": Mule::p('log.stream.name'),
    "auditEntry": {
        "instance": Mule::p('app.log.instance'),
        "id": messageId,
        "flow": flow,
        "identifier": identifier,
        "requestBody": if (direction == "Request" and level == "DEBUG" ) write(payload, "application/json") else  "",
        "responseBody": if (direction == "Response" and level == "DEBUG" ) write(payload, "application/json") else  "",
        "requestPath": if (direction == "Request") (attributes.requestPath default "") else "",
        "requestMethod": if (direction == "Request") (attributes.requestMethod default "") else "",
		"queryString": logTrim(if (direction == "Request") (attributes.queryString default "") else ""),
        "responseCode": if (direction == "Exception") (responseCode default "") else "",
        "environment": Mule::p('env'),
        "destination": if (direction == "Request") (attributes.destination default "") else "",
        "source": Mule::p('app.name'),
        "result": result,
		"message": logTrim(message ++ (if (!(["Request", "Response"] contains direction) and !isEmpty(payload)) write(payload, "application/json") else "")),
        "level": level,
        "direction": direction,
        "stackTrace": if(!isEmpty(payload)) write(payload, "application/java") else "",
        "timestamp": now() as String {format: dateFormat}
        }
}