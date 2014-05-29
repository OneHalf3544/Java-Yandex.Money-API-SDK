package ru.yandex.money.api;

import java.net.URI;
import java.util.Collections;
import java.util.Map;

/**
 * <p/>
 * <p/>
 * Created: 26.10.13 11:16
 * <p/>
 *
 * @author OneHalf
 */
public interface CommandUrlHolder {

    CommandUrlHolder DEFAULT = new ConstantUrlHolder(ApiCommandsFacade.URI_YM_API);

    URI getUrlForCommand(String commandName);

    Map<String, String> getAdditionalParams();

    class ConstantUrlHolder implements CommandUrlHolder {

        private final String uriYmApi;

        public ConstantUrlHolder(String uriYmApi1) {
            uriYmApi = uriYmApi1;
        }

        @Override
        public URI getUrlForCommand(String commandName) {
            return URI.create(uriYmApi + '/' + commandName);
        }

        @Override
        public Map<String, String> getAdditionalParams() {
            return Collections.emptyMap();
        }
    }
}
