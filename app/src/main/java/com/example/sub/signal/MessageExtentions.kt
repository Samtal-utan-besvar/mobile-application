package com.example.sub.signal

import org.json.JSONObject
import org.webrtc.IceCandidate
import org.webrtc.SessionDescription


/*
 This file is meant for adding functions to signal messages. Mainly functions for conversions
 between the SignalMessages and relevant data types.
 */


//region CallSignalMessage

/**
 * Alternative constructor for [CallMessage] that can be used directly with
 * [SessionDescription], no string conversion needed.
 */
fun CallMessage(
    SENDER_PHONE_NUMBER: String,
    RECEIVER_PHONE_NUMBER: String,
    sdp: SessionDescription
): CallMessage =
    CallMessage(
        SENDER_PHONE_NUMBER,
        RECEIVER_PHONE_NUMBER,
        sdp.description)


/**
 * Returns a [CallResponseMessage] with the given [CallResponse] based on the same phone
 * numbers as the [CallMessage]. If the call response is [CallResponse.ACCEPT],
 * a [SessionDescription] should also be passed.
 */
fun CallMessage.toResponse(callResponse: CallResponse, sdp: SessionDescription? = null): CallResponseMessage {
    return CallResponseMessage(callResponse, SENDER_PHONE_NUMBER, RECEIVER_PHONE_NUMBER, sdp)
}


/**
 * Returns a [SessionDescription] object constructed from the SDP description in the
 * [CallMessage].
 */
fun CallMessage.toSessionDescription() : SessionDescription {
    return SessionDescription(SessionDescription.Type.OFFER, SDP)
}

//endregion


//region CallResponseSignalMessage

/**
 * Alternative constructor for [CallResponseMessage] that can be used directly with
 * [CallResponse] and [SessionDescription], no string conversion needed.
 */
fun CallResponseMessage(
    callResponse: CallResponse,
    SENDER_PHONE_NUMBER: String,
    RECEIVER_PHONE_NUMBER: String,
    sdp: SessionDescription? = null): CallResponseMessage =
    CallResponseMessage(
        callResponse.toString(),
        SENDER_PHONE_NUMBER,
        RECEIVER_PHONE_NUMBER,
        if (sdp == null) "rick roll" else sdp.description)


/**
 * Returns true if the response of the [CallResponseMessage] is Accept, else false.
 */
fun CallResponseMessage.isAllowed(): Boolean{
    return RESPONSE.equals(CallResponse.ACCEPT.toString(), true)
}


/**
 * Returns a [SessionDescription] object constructed from the SDP description in the
 * [CallResponseMessage].
 */
fun CallResponseMessage.toSessionDescription() : SessionDescription {
    return SessionDescription(SessionDescription.Type.ANSWER, SDP)
}

//endregion


//region IceCandidateSignalMessage

/**
 * Alternative constructor for [IceCandidateMessage] that can be used directly with
 * [IceCandidate], no string conversion needed.
 */
fun IceCandidateMessage.Companion.fromIceCandidate(
    iceCandidate: IceCandidate,
    senderPhoneNumber: String,
    receiverPhoneNumber: String
): IceCandidateMessage {

    val jsonObject = JSONObject()
    jsonObject.put("sdpMid", iceCandidate.sdpMid)
    jsonObject.put("sdpMLineIndex", iceCandidate.sdpMLineIndex)
    jsonObject.put("sdp", iceCandidate.sdp)

    return IceCandidateMessage(
        senderPhoneNumber,
        receiverPhoneNumber,
        jsonObject.toString()
    )
}


/**
 * Returns an [IceCandidate] object constructed from the candidate description in the
 * [IceCandidateMessage].
 */
fun IceCandidateMessage.toIceCandidate(): IceCandidate {
    val json = JSONObject(CANDIDATE)
    return IceCandidate(
        json.getString("sdpMid"),
        json.getInt("sdpMLineIndex"),
        json.getString("sdp")
    )
}

//endregion