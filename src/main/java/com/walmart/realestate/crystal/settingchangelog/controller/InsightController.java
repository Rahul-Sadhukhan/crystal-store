package com.walmart.realestate.crystal.settingchangelog.controller;

import com.walmart.realestate.crystal.settingchangelog.controller.hypermedia.InsightAssembler;
import com.walmart.realestate.crystal.settingchangelog.model.Insight;
import com.walmart.realestate.crystal.settingchangelog.service.InsightService;
import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RequiredArgsConstructor
@RestController
@RequestMapping("/insights")
public class InsightController {

    private final InsightService insightService;

    private final InsightAssembler insightAssembler;

    @PostMapping
    @PreAuthorize("hasPolicy(#insight, 'createStoreReview')")
    public EntityModel<Insight> createInsight(@RequestBody @Valid Insight insight) {
        return insightAssembler.toModel(insightService.createInsight(insight));
    }

    @PostMapping(value = "/bulk", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasPolicy('createStoreReview')")
    public CollectionModel<EntityModel<Insight>> createInsights(@RequestBody @Valid List<Insight> insightList) {
        return insightAssembler.toCollectionModel(insightService.createInsights(insightList));
    }

    @GetMapping
    public CollectionModel<EntityModel<Insight>> getInsights(@RequestParam(required = false) Long storeNumber,
                                                             @RequestParam(required = false) String assetId) {
        return insightAssembler.toCollectionModel(insightService.getInsights(storeNumber, assetId))
                .add(linkTo(methodOn(InsightController.class)
                        .getInsights(storeNumber, assetId))
                        .withSelfRel());
    }

    @GetMapping("{insightId}")
    @PostAuthorize("hasPolicy(returnObject.getContent(), 'viewStoreReview')")
    public EntityModel<Insight> getInsight(@PathVariable String insightId) {
        return insightAssembler.toModel(insightService.getInsight(insightId));
    }

}
