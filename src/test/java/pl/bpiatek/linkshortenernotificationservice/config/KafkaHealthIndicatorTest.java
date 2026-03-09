package pl.bpiatek.linkshortenernotificationservice.config;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.DescribeClusterOptions;
import org.apache.kafka.clients.admin.DescribeClusterResult;
import org.apache.kafka.common.KafkaFuture;
import org.apache.kafka.common.Node;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.actuate.health.Status;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class KafkaHealthIndicatorTest {

    @Mock
    private AdminClient adminClient;

    @Mock
    private DescribeClusterResult describeClusterResult;

    @Mock
    private KafkaFuture<Collection<Node>> nodesFuture;

    @InjectMocks
    private KafkaHealthIndicator healthIndicator;

    @Test
    void shouldReturnUpWhenKafkaIsHealthy() throws Exception {
        // given
        var node = new Node(1, "localhost", 9092);

        when(adminClient.describeCluster(any(DescribeClusterOptions.class)))
                .thenReturn(describeClusterResult);
        when(describeClusterResult.nodes())
                .thenReturn(nodesFuture);
        when(nodesFuture.get(eq(3L), eq(TimeUnit.SECONDS)))
                .thenReturn(List.of(node));

        // when
        var health = healthIndicator.health();

        // then
        assertThat(health.getStatus()).isEqualTo(Status.UP);
        assertThat(health.getDetails()).containsEntry("nodeCount", 1);
    }

    @Test
    void shouldReturnDownWhenKafkaTimesOut() throws Exception {
        // given
        when(adminClient.describeCluster(any(DescribeClusterOptions.class)))
                .thenReturn(describeClusterResult);
        when(describeClusterResult.nodes())
                .thenReturn(nodesFuture);
        when(nodesFuture.get(eq(3L), eq(TimeUnit.SECONDS)))
                .thenThrow(new TimeoutException("Simulated network timeout"));

        // when
        var health = healthIndicator.health();

        // then
        assertThat(health.getStatus()).isEqualTo(Status.DOWN);
        assertThat(health.getDetails()).containsEntry("error", "Cannot connect to Kafka broker");
    }

    @Test
    void shouldReturnDownWhenClusterIsEmpty() throws Exception {
        // given
        when(adminClient.describeCluster(any(DescribeClusterOptions.class)))
                .thenReturn(describeClusterResult);
        when(describeClusterResult.nodes())
                .thenReturn(nodesFuture);
        when(nodesFuture.get(eq(3L), eq(TimeUnit.SECONDS)))
                .thenReturn(List.of());

        // when
        var health = healthIndicator.health();

        // then
        assertThat(health.getStatus()).isEqualTo(Status.DOWN);
        assertThat(health.getDetails()).containsEntry("error", "No nodes found in cluster");
    }
}