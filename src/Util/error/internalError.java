package Util.error;

import Util.position;

public class internalError extends Error {

    public internalError(String msg) {
        super("Internal Error:" + msg, new position(0, 0));
    }

}
