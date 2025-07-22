package es.udc.OpenHope.dto;

import lombok.Data;
@Data
public class BankAccountParams {
    private String resourceId;
    private String iban;
    private String name;
    private String ownerName;
    private String originalName;
    private AspspParamsDto aspsp;
}
