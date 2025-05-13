package com.complaintandfeedback.Model;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Category_Mst {

    @Size(max = 16)
    private String category_id;

    @NotNull
    @Size(max = 100)
    private String category_name;

    @Size(max = 255)
    private String description;

    @NotNull
    @Size(max = 16)
    private String department_id;

    @NotNull
    private Long org_id;

    private Long opr_id;

    private String created_on;

    @Size(max = 3)
    private String is_active;
    private String created_by;
    private String modified_by;
}
