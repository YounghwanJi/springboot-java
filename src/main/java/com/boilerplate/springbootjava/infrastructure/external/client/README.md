# Information
### 각 External Service에 대해 여기에서 RestClient를 분리.
- ItemExternalRestClient: Item server로의 요청.
- ex) ProductExternalRestClient: 필요 시, 추가하여 사용. 이 경우, 각각 아래의 파일들 추가 필요.
    - common/exception/errorcode/ProductExternalApiErrorCode.java
    - common/exception/errorcode/mapper/ProductExternalApiEerrorCodeMapper.java

### 외부 HTTP 호출이 3개를 초과하면, Abstract type 분리를 고려한다.

#### 예시

**`AbstractExternalClient.java`**
```java
@RequiredArgsConstructor
public abstract class AbstractExternalClient {

    private final RestClient.Builder restClientBuilder;

    protected abstract String baseUrl();
    protected abstract ExternalApiErrorCodeMapper errorCodeMapper();

    /** 필요 시 헤더 확장용 */
    protected Map<String, String> defaultHeaders() {
        return Map.of();
    }

    /** 🔥 Template Method */
    protected <T, R> R execute(
            ExternalRequestSpec<T> request,
            Class<R> responseType
    ) {
        RestClient client = restClientBuilder
                .baseUrl(baseUrl())
                .build();

        RestClient.RequestBodySpec requestSpec =
                client.method(request.getMethod())
                      .uri(uriBuilder -> {
                          uriBuilder.path(request.getPath());
                          if (request.getQueryParams() != null) {
                              request.getQueryParams()
                                     .forEach(uriBuilder::queryParam);
                          }
                          return uriBuilder.build();
                      });

        // default headers
        defaultHeaders().forEach(requestSpec::header);

        // request headers
        if (request.getHeaders() != null) {
            request.getHeaders().forEach(requestSpec::header);
        }

        if (request.getBody() != null) {
            requestSpec.body(request.getBody());
        }

        return requestSpec
                .retrieve()
                .onStatus(
                        HttpStatusCode::isError,
                        (req, res) -> {
                            HttpStatus status =
                                    HttpStatus.valueOf(res.getStatusCode().value());

                            throw new CustomException(
                                    errorCodeMapper().map(status)
                            );
                        }
                )
                .body(responseType);
    }
}

```

**`PaymentExternalClient`**
```java
@Component
public class PaymentExternalClient extends AbstractExternalClient {

    private final PaymentApiErrorCodeMapper paymentErrorCodeMapper;

    public PaymentExternalClient(
            RestClient.Builder restClientBuilder,
            PaymentApiErrorCodeMapper paymentErrorCodeMapper
    ) {
        super(restClientBuilder);
        this.paymentErrorCodeMapper = paymentErrorCodeMapper;
    }

    @Override
    protected String baseUrl() {
        return "https://api.payment.com";
    }

    @Override
    protected ExternalApiErrorCodeMapper errorCodeMapper() {
        return paymentErrorCodeMapper;
    }

    @Override
    protected Map<String, String> defaultHeaders() {
        return Map.of(
                "Content-Type", "application/json",
                "X-Client-Id", "payment-service"
        );
    }

    public PaymentResponse pay(PaymentRequest request) {
        ExternalRequestSpec<PaymentRequest> spec =
                ExternalRequestSpec.<PaymentRequest>builder()
                        .path("/payments")
                        .method(HttpMethod.POST)
                        .body(request)
                        .build();

        return execute(spec, PaymentResponse.class);
    }
}

```
**`OrderExternalClient`**
```java
@Component
public class OrderExternalClient extends AbstractExternalClient {

    private final OrderApiErrorCodeMapper orderErrorCodeMapper;

    public OrderExternalClient(
            RestClient.Builder restClientBuilder,
            OrderApiErrorCodeMapper orderErrorCodeMapper
    ) {
        super(restClientBuilder);
        this.orderErrorCodeMapper = orderErrorCodeMapper;
    }

    @Override
    protected String baseUrl() {
        return "https://api.order.com";
    }

    @Override
    protected ExternalApiErrorCodeMapper errorCodeMapper() {
        return orderErrorCodeMapper;
    }

    public OrderDetailResponse getOrder(String orderId) {
        ExternalRequestSpec<Void> spec =
                ExternalRequestSpec.<Void>builder()
                        .path("/orders/" + orderId)
                        .method(HttpMethod.GET)
                        .build();

        return execute(spec, OrderDetailResponse.class);
    }
}
```

**`OrderService`**
```java
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderExternalClient orderExternalClient;

    public OrderDetailResponse findOrder(String orderId) {
        return orderExternalClient.getOrder(orderId);
    }
}

```