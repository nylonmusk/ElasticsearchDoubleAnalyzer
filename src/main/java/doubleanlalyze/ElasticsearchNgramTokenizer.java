package doubleanlalyze;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import config.ElasticConfiguration;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.AnalyzeRequest;
import org.elasticsearch.client.indices.AnalyzeResponse;
import org.elasticsearch.common.xcontent.XContentType;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class ElasticsearchNgramTokenizer {

    public static void main(String[] args) {
        ElasticConfiguration elasticConfiguration = new ElasticConfiguration("localhost", 9200, "", "http");
        RestHighLevelClient client = elasticConfiguration.getElasticClient();
        ObjectMapper mapper = new ObjectMapper();

        try {
            // JSON 파일 읽기
//            String jsonContent = new String(Files.readAllBytes(Paths.get("/Users/nylonmusk/Downloads/Elasticsearch/config/title_content_data.json")));
            String jsonContent = new String(Files.readAllBytes(Paths.get("/Users/nylonmusk/Downloads/Elasticsearch/config/title_content_data_small.json")));
            JsonNode rootNode = mapper.readTree(jsonContent);
            JsonNode entries = rootNode.path("entries");

            // Title과 Content 결합


            // 분석 요청
            AnalyzeRequest request = AnalyzeRequest.withField("nouns", "text", jsonContent);
            AnalyzeResponse response = client.indices().analyze(request, RequestOptions.DEFAULT);

            // 분석 결과에서 토큰 추출
            List<String> tokens = new ArrayList<>();
            for (AnalyzeResponse.AnalyzeToken token : response.getTokens()) {
                tokens.add(token.getTerm());
            }


            // 추출된 토큰을 ngrams_index에 색인
            for (String token : tokens) {
                IndexRequest indexRequest = new IndexRequest("ngrams_index");
                indexRequest.source("{\"ngram_text\": \"" + token + "\"}", XContentType.JSON);
                IndexResponse indexResponse = client.index(indexRequest, RequestOptions.DEFAULT);
                System.out.println("Document indexed. ID: " + indexResponse.getId());
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            // 클라이언트 종료
            try {
                client.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
