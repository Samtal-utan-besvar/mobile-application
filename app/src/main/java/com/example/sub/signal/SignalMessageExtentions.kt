package com.example.sub.signal

import org.json.JSONObject
import org.webrtc.IceCandidate
import org.webrtc.SessionDescription


/*
 This file is meant for adding functions to SignalMessages. Mainly functions for conversions
 between the SignalMessages and relevant data types.
 */


//region CallSignalMessage

/**
 * Alternative constructor for [CallSignalMessage] that can be used directly with
 * [SessionDescription], no string conversion needed.
 */
fun CallSignalMessage(
    CALLER_PHONE_NUMBER: String,
    TARGET_PHONE_NUMBER: String,
    sdp: SessionDescription
): CallSignalMessage =
    CallSignalMessage(
        CALLER_PHONE_NUMBER,
        TARGET_PHONE_NUMBER,
        sdp.description)


/**
 * Returns a [CallResponseSignalMessage] with the given [CallResponse] based on the same phone
 * numbers as the [CallSignalMessage]. If the call response is [CallResponse.ACCEPT],
 * a [SessionDescription] should also be passed.
 */
fun CallSignalMessage.toResponse(callResponse: CallResponse, sdp: SessionDescription? = null): CallResponseSignalMessage {
    return CallResponseSignalMessage(callResponse, CALLER_PHONE_NUMBER, TARGET_PHONE_NUMBER, sdp)
}

//endregion


//region CallResponseSignalMessage

/**
 * Alternative constructor for [CallResponseSignalMessage] that can be used directly with
 * [CallResponse] and [SessionDescription], no string conversion needed.
 */
fun CallResponseSignalMessage(
    callResponse: CallResponse,
    CALLER_PHONE_NUMBER: String,
    TARGET_PHONE_NUMBER: String,
    sdp: SessionDescription? = null): CallResponseSignalMessage =
    CallResponseSignalMessage(
        callResponse.toString(),
        CALLER_PHONE_NUMBER,
        TARGET_PHONE_NUMBER,
        if (sdp == null) "rick roll" else sdp.description)


/**
 * Returns true if the response of the [CallResponseSignalMessage] is Accept, else false.
 */
fun CallResponseSignalMessage.isAllowed(): Boolean{
    return RESPONSE.equals(CallResponse.ACCEPT.toString(), true)
}


/**
 * Returns a [SessionDescription] object constructed from the SDP description in the
 * [CallResponseSignalMessage].
 */
fun CallResponseSignalMessage.toSessionDescription(type: SessionDescription.Type) : SessionDescription {
    return SessionDescription(type, SDP)
}

//endregion


//region IceCandidateSignalMessage

/**
 * Alternative constructor for [IceCandidateSignalMessage] that can be used directly with
 * [IceCandidate], no string conversion needed.
 */
fun IceCandidateSignalMessage.Companion.fromIceCandidate(
    iceCandidate: IceCandidate,
    originPhoneNumber: String,
    targetPhoneNumber: String
): IceCandidateSignalMessage {

    val jsonObject = JSONObject()
    jsonObject.put("sdpMid", iceCandidate.sdpMid)
    jsonObject.put("sdpMLineIndex", iceCandidate.sdpMLineIndex)
    jsonObject.put("sdp", iceCandidate.sdp)

    return IceCandidateSignalMessage(
        originPhoneNumber,
        targetPhoneNumber,
        jsonObject.toString()
    )
}


/**
 * Returns an [IceCandidate] object constructed from the candidate description in the
 * [IceCandidateSignalMessage].
 */
fun IceCandidateSignalMessage.toIceCandidate(): IceCandidate {
    val json = JSONObject(CANDIDATE)
    return IceCandidate(
        json.getString("sdpMid"),
        json.getInt("sdpMLineIndex"),
        json.getString("sdp")
    )
}

//endregion