package io.github.x0b.safdav;

import android.content.Context;

import java.io.IOException;

import io.github.x0b.safdav.file.SafConstants;

/**
 * Saf Emulation Server
 */
public class SafAccessProvider {

    private static SafDAVServer davServer;
    private static SafDirectServer directServer;

    public static SafDAVServer getServer(Context context){
        if(null == davServer){
            davServer = new SafDAVServer(SafConstants.SAF_REMOTE_PORT, context);
            try {
                davServer.start();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return davServer;
    }

    public static SafDirectServer getDirectServer(Context context) {
        if(null == directServer) {
            directServer = new SafDirectServer(context);
        }
        return directServer;
    }

}
