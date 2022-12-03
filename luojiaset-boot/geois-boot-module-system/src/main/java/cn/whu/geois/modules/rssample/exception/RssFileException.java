package cn.whu.geois.modules.rssample.exception;

/**
 * @author czp
 * @version 1.0
 * @date 2021/1/28 21:02
 */
public class RssFileException extends RuntimeException{
    public RssFileException(String message) {
        super(message);
    }
    public RssFileException(String message, Throwable cause) {
        super(message, cause);
    }
}
