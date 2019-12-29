package com.codeyard.teleprompter;

import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.v2.DbxClientV2;

class DropboxClient {

    static DbxClientV2 getClient(String ACCESS_TOKEN) {
        // Create DropBox client
        //noinspection deprecation
        DbxRequestConfig config = new DbxRequestConfig("dropbox/sample-app", "en_US");
        return new DbxClientV2(config, ACCESS_TOKEN);
    }
}