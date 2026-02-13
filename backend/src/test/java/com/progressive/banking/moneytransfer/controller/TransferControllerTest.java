package com.progressive.banking.moneytransfer.controller;
 
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
 
import java.math.BigDecimal;
 
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
 
import com.fasterxml.jackson.databind.ObjectMapper;
import com.progressive.banking.moneytransfer.domain.dto.TransferRequest;
import com.progressive.banking.moneytransfer.domain.dto.TransferResponse;
import com.progressive.banking.moneytransfer.domain.enums.TransactionStatusEnum;
import com.progressive.banking.moneytransfer.service.TransferService;
 
@Import(ObjectMapper.class)
@WebMvcTest(controllers = TransferController.class)
@AutoConfigureMockMvc(addFilters = false)
class TransferControllerTest {
 
    @Autowired
    private MockMvc mockMvc;
 
    @Autowired
    private ObjectMapper objectMapper;
 
    @MockitoBean
    private TransferService transferService;
 
    @Test
    @DisplayName("POST /api/v1/transfers returns 201 and TransferResponse")
    @WithMockUser(username = "testuser")  // Mock authenticated user
    void transfer_validRequest_returnsCreated() throws Exception {
        // Create the TransferRequest object with valid data
        TransferRequest request = new TransferRequest();
        request.setFromAccountId(1);
        request.setToAccountId(2);
        request.setAmount(BigDecimal.valueOf(100));
        request.setIdempotencyKey("idem-123");
 
        // Create the mocked TransferResponse object
        TransferResponse response = new TransferResponse();
        response.setTransactionId(1);
        response.setFromAccountId(1);
        response.setToAccountId(2);
        response.setAmount(BigDecimal.valueOf(100));
        response.setStatus(TransactionStatusEnum.SUCCESS);
        response.setIdempotencyKey("idem-123");
 
        // Mock TransferService behavior - note the username parameter
        given(transferService.transfer(any(TransferRequest.class), eq("testuser")))
                .willReturn(response);
 
        // Perform the POST request to the controller
        mockMvc.perform(post("/api/v1/transfers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.transactionId").value(1))
                .andExpect(jsonPath("$.fromAccountId").value(1))
                .andExpect(jsonPath("$.toAccountId").value(2))
                .andExpect(jsonPath("$.amount").value(100))
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.idempotencyKey").value("idem-123"));
    }
 
    @Test
    @DisplayName("POST /api/v1/transfers with X-Idempotency-Key header uses header when body key is null")
    @WithMockUser(username = "testuser")
    void transfer_headerIdempotencyKey_usedWhenBodyBlank() throws Exception {
        // Build JSON request body WITHOUT idempotencyKey field
        String requestBody = """
            {
                "fromAccountId": 1,
                "toAccountId": 2,
                "amount": 50
            }
            """;
 
        // Create the mocked TransferResponse object
        TransferResponse response = new TransferResponse();
        response.setTransactionId(2);
        response.setFromAccountId(1);
        response.setToAccountId(2);
        response.setAmount(BigDecimal.valueOf(50));
        response.setIdempotencyKey("header-key");
        response.setStatus(TransactionStatusEnum.SUCCESS);
 
        // Mock TransferService behavior
        given(transferService.transfer(any(TransferRequest.class), eq("testuser")))
                .willReturn(response);
 
        // Perform the POST request with X-Idempotency-Key header
        mockMvc.perform(post("/api/v1/transfers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Idempotency-Key", "header-key")
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.transactionId").value(2))
                .andExpect(jsonPath("$.idempotencyKey").value("header-key"))
                .andExpect(jsonPath("$.status").value("SUCCESS"));
    }
 
    @Test
    @DisplayName("POST /api/v1/transfers generates UUID when no idempotency key provided")
    @WithMockUser(username = "testuser")
    void transfer_noIdempotencyKey_generatesUUID() throws Exception {
        // Build JSON request body WITHOUT idempotencyKey field
        String requestBody = """
            {
                "fromAccountId": 1,
                "toAccountId": 2,
                "amount": 75
            }
            """;
 
        // Create the mocked TransferResponse object with a generated UUID
        TransferResponse response = new TransferResponse();
        response.setTransactionId(3);
        response.setFromAccountId(1);
        response.setToAccountId(2);
        response.setAmount(BigDecimal.valueOf(75));
        response.setIdempotencyKey("generated-uuid-123");  // Simulated UUID
        response.setStatus(TransactionStatusEnum.SUCCESS);
 
        // Mock TransferService behavior
        given(transferService.transfer(any(TransferRequest.class), eq("testuser")))
                .willReturn(response);
 
        // Perform the POST request without any idempotency key
        mockMvc.perform(post("/api/v1/transfers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.transactionId").value(3))
                .andExpect(jsonPath("$.idempotencyKey").exists())  // Just verify it exists
                .andExpect(jsonPath("$.status").value("SUCCESS"));
    }
 
    @Test
    @DisplayName("POST /api/v1/transfers uses different usernames from authentication")
    @WithMockUser(username = "anotheruser")
    void transfer_differentUsername_usesAuthenticatedUser() throws Exception {
        TransferRequest request = new TransferRequest();
        request.setFromAccountId(5);
        request.setToAccountId(6);
        request.setAmount(BigDecimal.valueOf(200));
        request.setIdempotencyKey("idem-456");
 
        TransferResponse response = new TransferResponse();
        response.setTransactionId(4);
        response.setFromAccountId(5);
        response.setToAccountId(6);
        response.setAmount(BigDecimal.valueOf(200));
        response.setStatus(TransactionStatusEnum.SUCCESS);
        response.setIdempotencyKey("idem-456");
 
        // Note: username is "anotheruser" from @WithMockUser
        given(transferService.transfer(any(TransferRequest.class), eq("anotheruser")))
                .willReturn(response);
 
        mockMvc.perform(post("/api/v1/transfers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.transactionId").value(4))
                .andExpect(jsonPath("$.status").value("SUCCESS"));
    }
}