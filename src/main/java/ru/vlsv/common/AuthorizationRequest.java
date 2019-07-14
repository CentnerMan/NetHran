package ru.vlsv.common;

/**
 * Java, NetHran.
 *
 * @author Anatoly Lebedev
 * @version 1.0.0 14.07.2019
 * @link https://github.com/Centnerman
 */

public class AuthorizationRequest extends AbstractMessage{

    private String name;
    private String password;

    public String getName() {
        return name;
    }

    public String getPassword() {
        return password;
    }

    public AuthorizationRequest(String name, String password) {
        this.name = name;
        this.password = password;
    }
}
