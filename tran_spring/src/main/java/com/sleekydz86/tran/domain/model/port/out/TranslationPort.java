package com.sleekydz86.tran.domain.model.port.out;

import com.sleekydz86.tran.domain.model.record.Translation;
import com.sleekydz86.tran.domain.model.record.TranslationRequest;

public interface TranslationPort {


    Translation translate(TranslationRequest request);


    boolean isAvailable();

    String getUsageInfo();

}
