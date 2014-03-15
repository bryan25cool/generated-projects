/*
 * (c) Copyright 2005-2013 JAXIO, http://www.jaxio.com
 * Source code generated by Celerio, a Jaxio product
 * Want to purchase Celerio ? email us at info@jaxio.com
 * Follow us on twitter: @springfuse
 * Documentation: http://www.jaxio.com/documentation/celerio/
 * Template pack-jsf2-spring-conversation:src/main/java/domain/LazyDataModel.e.vm.java
 */
package com.jaxio.appli.web.domain;

import javax.inject.Inject;
import javax.inject.Named;

import org.primefaces.model.LazyDataModel;

import com.jaxio.appli.domain.SavedSearch;
import com.jaxio.appli.repository.SavedSearchRepository;
import com.jaxio.appli.web.domain.support.GenericLazyDataModel;
import com.jaxio.appli.web.faces.ConversationContextScoped;

/**
 * Provide PrimeFaces {@link LazyDataModel} for {@link SavedSearch}
 */
@Named
@ConversationContextScoped
public class SavedSearchLazyDataModel extends GenericLazyDataModel<SavedSearch, Integer, SavedSearchSearchForm> {
    private static final long serialVersionUID = 1L;

    @Inject
    public SavedSearchLazyDataModel(SavedSearchRepository savedSearchRepository, SavedSearchController savedSearchController,
            SavedSearchSearchForm savedSearchSearchForm, SavedSearchExcelExporter savedSearchExcelExporter) {
        super(savedSearchRepository, savedSearchController, savedSearchSearchForm, savedSearchExcelExporter);
    }
}