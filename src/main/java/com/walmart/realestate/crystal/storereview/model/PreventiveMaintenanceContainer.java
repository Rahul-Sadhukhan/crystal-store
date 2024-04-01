package com.walmart.realestate.crystal.storereview.model;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PreventiveMaintenanceContainer implements Serializable {

    private List<PreventiveMaintenanceReadyToStart> pmReadyToStartList;

    private Long totalCount;

    private Long agingCount;

}
