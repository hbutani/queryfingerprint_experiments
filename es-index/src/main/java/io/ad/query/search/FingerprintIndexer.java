package io.ad.query.search;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.BulkRequest;
import co.elastic.clients.elasticsearch.core.BulkResponse;
import co.elastic.clients.elasticsearch.core.bulk.BulkResponseItem;
import co.elastic.clients.elasticsearch.indices.CreateIndexRequest;
import co.elastic.clients.elasticsearch.indices.CreateIndexResponse;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import com.google.common.collect.ImmutableList;
import gudusoft.gsqlparser.EDbVendor;
import io.ad.query.search.utils.TPCDSSQLEnv;
import io.ad.query.search.utils.Utils;
import org.apache.http.HttpHost;
import org.apache.log4j.Logger;
import org.elasticsearch.client.RestClient;
import org.hatke.queryfingerprint.model.Queryfingerprint;
import org.hatke.queryfingerprint.snowflake.parse.QueryAnalysis;
import org.hatke.queryfingerprint.snowflake.parse.QueryfingerprintBuilder;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;

public class FingerprintIndexer {

    private static Logger logger = Logger.getLogger(FingerprintIndexer.class);

    private static TPCDSSQLEnv sqlEnv = new TPCDSSQLEnv(EDbVendor.dbvsnowflake);

    public static void main(String[] args) throws IOException {
        ElasticsearchClient client = getElasticClient();
//        createIndex(client);
        insertTpcdsFingerprint(client);
    }

    private static void insertTpcdsFingerprint(ElasticsearchClient client) throws IOException {
        BulkRequest.Builder br = new BulkRequest.Builder();
        for (int i = 1; i < 103; i++) {
            try {
                ImmutableList<Queryfingerprint> fingerprints = getQueryFingerprint("query" + i);
                for (Queryfingerprint qf : fingerprints) {
                    br.operations(op -> op
                            .index(idx -> idx
                                    .index("fingerprints")
                                    .id(qf.getHash().toString())
                                    .document(qf)
                            )
                    );
                }
            } catch (IllegalArgumentException e) {
            }
        }

        BulkResponse result = client.bulk(br.build());
        if (result.errors()) {
            logger.error("Bulk had errors");
            for (BulkResponseItem item : result.items()) {
                if (item.error() != null) {
                    logger.error(item.error().reason());
                }
            }
        }
    }

    private static ImmutableList<Queryfingerprint> getQueryFingerprint(String name) throws IOException {
        QueryAnalysis qa = new QueryAnalysis(sqlEnv, Utils.readTpcdsQuery(name));
        QueryfingerprintBuilder qfpB = new QueryfingerprintBuilder(qa);
        ImmutableList<Queryfingerprint> fps = qfpB.build();
        return fps;
    }

    private static void createIndex(ElasticsearchClient client) throws IOException {
        String content = Utils.readResourceFile("elastic/index.json");
        CreateIndexRequest indexRequest = new CreateIndexRequest.Builder()
                .index("fingerprints")
                .withJson(new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8)))
                .build();
        CreateIndexResponse createIndexResponse = client.indices().create(indexRequest);
        System.out.printf(createIndexResponse.toString());
    }

    private static ElasticsearchClient getElasticClient() {
        RestClient restClient = RestClient.builder(
                new HttpHost("localhost", 9200)).build();

        // Create the transport with a Jackson mapper
        ElasticsearchTransport transport = new RestClientTransport(
                restClient, new JacksonJsonpMapper());

        // And create the API client
        return new ElasticsearchClient(transport);
    }
}
