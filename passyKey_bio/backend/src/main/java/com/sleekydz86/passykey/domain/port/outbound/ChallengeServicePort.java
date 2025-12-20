package com.sleekydz86.passykey.domain.port.outbound;

import com.webauthn4j.data.client.challenge.Challenge;

public interface ChallengeServicePort {
    Challenge generateAndStoreChallenge(String sessionId, String type);
    Challenge getChallenge(String sessionId, String type);
    void removeChallenge(String sessionId, String type);
}
