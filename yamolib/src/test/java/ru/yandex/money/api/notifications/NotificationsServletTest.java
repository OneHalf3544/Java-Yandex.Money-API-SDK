package ru.yandex.money.api.notifications;

import com.google.common.collect.ImmutableMap;
import org.junit.Test;
import ru.yandex.money.api.YamoneyAccount;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.Date;
import java.util.Map;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.beans.HasPropertyWithValue.hasProperty;
import static org.hamcrest.core.Is.is;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.*;

public class NotificationsServletTest {


    private NotificationsServlet instance = new NotificationsServlet();

    @Test
    public void testListener() throws Exception {
        IncomingTransferListener listener = mock(IncomingTransferListener.class);
        NotificationsServlet.setListener(listener);
        NotificationsServlet.setSecret("0UyvT/YmMb9ed8FA6rsrYXqP");

        HttpServletRequest request = createRequest(new ImmutableMap.Builder<String, String>()
                .put("notification_type", "p2p-incoming")
                .put("operation_id", "818163584552108017")
                .put("amount", "2.23")
                .put("currency", "643")
                .put("datetime", "2012-12-17T17:49:52Z")
                .put("sender", "410011608243693")
                .put("codepro", "false")
                .put("label", "12625")
                .put("sha1_hash", "b9d4dee98caec486a8a3b1a577fce7efd0e7f0fb")
                .build());

        instance.doPost(request, mock(HttpServletResponse.class, RETURNS_DEEP_STUBS));

        verify(listener).processNotification((IncomingTransfer) argThat(allOf(
                hasProperty("operationId", is("818163584552108017")),
                hasProperty("amount", is(BigDecimal.valueOf(2.23))),
                hasProperty("currency", is(643)),
                hasProperty("datetime", is(new Date(1355766592000L))),
                hasProperty("sender", is(new YamoneyAccount("410011608243693"))),
                hasProperty("codepro", is(false)),
                hasProperty("label", is("12625"))
        )));
    }

    private HttpServletRequest createRequest(Map<String, String> map) {
        HttpServletRequest mock = mock(HttpServletRequest.class);
        when(mock.getParameterNames()).thenReturn(Collections.enumeration(map.keySet()));
        for (Map.Entry<String, String> entry : map.entrySet()) {
            when(mock.getParameter(entry.getKey())).thenReturn(entry.getValue());
        }
        return mock;
    }
}