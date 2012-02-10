/*
 * (c) Copyright 2005-2012 JAXIO, www.jaxio.com
 * Source code generated by Celerio, a Jaxio product
 * Want to use Celerio within your company? email us at info@jaxio.com
 * Follow us on twitter: @springfuse
 * Template pack-jsf2-primefaces-sd:src/main/java/flow/FlowsMenuHandler.p.vm.java
 */
package com.jaxio.demo.web.flow;

import static com.google.common.collect.Lists.newArrayList;

import java.io.Serializable;
import java.util.List;

import org.primefaces.component.menuitem.MenuItem;
import org.primefaces.model.DefaultMenuModel;
import org.primefaces.model.MenuModel;

import org.springframework.data.domain.Persistable;
import org.springframework.webflow.execution.RequestContext;

/**
 * Store the 'active flows'. This class is used from the xml flows to add/update meta-information 
 * about the current flow (label, current url)
 * It is used from the view to obtain the 'active flows' as a primefaces Menu.
 * 
 * Note: No annotation is used for this bean as it is Session bean defined as a scoped-proxy. See springmvc-parent.xml conf file.
 */
public class FlowsMenuHandler implements Serializable {
    static final private long serialVersionUID = 1L;

    private int maxExecutions = 5;
    private String defaultExternalRedirect = "index.html";
    private String defaultErrorExternalRedirect = "/error";

    private List<FlowMenuEntry> flowMenuEntries = newArrayList();

    public void setDefaultExternalRedirect(String defaultExternalRedirect) {
        this.defaultExternalRedirect = defaultExternalRedirect;
    }

    /**
     * NOTE: Should match exactly the max-executions param of the flow executor,
     * otherwise it leads to some bugs as webflow silently discard flows (FIFO)
     * if the number of active conversations exceeds max-executions.
     */
    public void setMaxExecutions(int maxExecutions) {
        this.maxExecutions = maxExecutions;
    }

    //-----------------------------------------------
    // Invoked from web flow listener
    //-----------------------------------------------
    public void removeMenu(RequestContext context) {
        String flowId = getFlowModuleId(context);
        String flowExecutionId = getFlowExecutionId(context);
        flowMenuEntries.remove(getFlowMenuEntry(flowId, flowExecutionId));
    }

    //-----------------------------------------------
    // Invoked from spring web flow's XML script.
    //-----------------------------------------------
    public void updateMenu(RequestContext context, String label) {
        updateMenu(context, null, label);
    }

    public void updateMenu(RequestContext context, Serializable entityId, String label) {
        String flowId = getFlowModuleId(context);
        String flowExecutionId = getFlowExecutionId(context);

        FlowMenuEntry flowMenuEntry = getFlowMenuEntry(flowId, flowExecutionId);

        if (flowMenuEntry == null) {
            // add a new entry at the end of the list
            flowMenuEntry = new FlowMenuEntry(flowId, flowExecutionId);
            flowMenuEntries.add(flowMenuEntry);

            // check max execution and eventually remove the oldest menu entry.
            if (maxExecutionsExceeded()) {
                flowMenuEntries.remove(0);
            }
        }

        if (label != null) {
            label = flowId + ": " + label;
        } else {
            label = flowId;
        }

        flowMenuEntry.setLabel(label);
        String contextPath = context.getExternalContext().getContextPath();
        String url = null;
        if (contextPath == null) {
            url = context.getFlowExecutionUrl();
        } else {
            url = context.getFlowExecutionUrl().substring(contextPath.length());
        }
        flowMenuEntry.setFlowExecutionUrl(url);
        flowMenuEntry.setEntityId(entityId);
    }

    public String getErrorEndStateRedirect() {
        return defaultErrorExternalRedirect;
    }

    public String getEndStateRedirect(RequestContext context) {
        return endStateRedirect(context, null);
    }

    public String endStateRedirect(RequestContext context, String extraParam) {
        String flowId = getFlowModuleId(context);
        List<FlowMenuEntry> flowMenu = getFlowMenuEntries(flowId);

        if (flowMenu.isEmpty()) {
            return defaultExternalRedirect;
        }

        StringBuilder result = new StringBuilder(flowMenu.get(flowMenu.size() - 1).getFlowExecutionUrl());
        if (extraParam != null && extraParam.length() > 0) {
            result.append("&").append(extraParam);
        }

        return result.toString();
    }

    /**
     * Used from your XML spring web flow script
     * to determine if the passed entity is already present in the menu of the current flow id.
     * If present, you should not open twice the entity but instead redirect the user to the existing
     * flow execution using the endStateRedirectToAlreadyOpenFlow method.
     *
     * @param entity the entity that the user wants to 'open'
     * @return true if the passed entity is already open.
     *
     * @see #endStateRedirectToAlreadyOpenFlow
     */
    public boolean isFlowAlreadyOpen(RequestContext context, Persistable<?> entity) {
        String flowId = getFlowModuleId(context);

        for (FlowMenuEntry fme : getFlowMenuEntries(flowId)) {
            if (fme.getEntityId() != null && entity.getId() != null && fme.getEntityId().equals(entity.getId())) {
                return true;
            }
        }

        return false;
    }

    /**
     * Redirect the user to the flow execution that corresponds to the passed entity.
     * Before calling this method you must first check that isFlowAlreadyOpen(entity) returns true.
     *
     * @param entity the entity that the user wants to 'open'
     * @return the URL of the flow execution that corresponds to the passed entity.
     */
    public String endStateRedirectToAlreadyOpenFlow(RequestContext context, Persistable<?> entity) {
        String flowId = getFlowModuleId(context);

        for (FlowMenuEntry fme : getFlowMenuEntries(flowId)) {
            if (fme.getEntityId() != null && entity.getId() != null && fme.getEntityId().equals(entity.getId())) {
                StringBuilder result = new StringBuilder(fme.getFlowExecutionUrl());
                return result.toString();
            }
        }

        return this.defaultExternalRedirect;
    }

    //-----------------------------------------------
    // Invoked from the view
    //-----------------------------------------------

    /**
     * Returns the menu holding active flows...
     */
    public MenuModel getActiveFlowsMenuModel(RequestContext context) {
        String flowId = getFlowModuleId(context);
        String flowExecutionId = getFlowExecutionId(context);

        MenuModel model = new DefaultMenuModel();

        for (FlowMenuEntry flowMenuEntry : getFlowMenuEntries()) {
            MenuItem htmlMenuItem = new MenuItem();
            htmlMenuItem.setValue(flowMenuEntry.getLabel());
            htmlMenuItem.setUrl(flowMenuEntry.getFlowExecutionUrl());

            if (flowMenuEntry.getFlowId().equals(flowId) && flowMenuEntry.getFlowExecutionId().equals(flowExecutionId)) {
                htmlMenuItem.setDisabled(true);
            } else {
                htmlMenuItem.setDisabled(false);
            }

            model.addMenuItem(htmlMenuItem);
        }

        return model;
    }

    //-----------------------------------------------
    // Internal helpers
    //-----------------------------------------------

    /**
     * Returns the base of the flow id.
     * If the flow id is "account/select", it returns "account".
     */
    private String getFlowModuleId(RequestContext context) {
        String flowId = context.getActiveFlow().getId();
        int index = flowId.indexOf('/');
        String flowModuleId = null;

        if (index > 0) {
            flowModuleId = flowId.substring(0, index);
        } else {
            flowModuleId = flowId;
        }

        return flowModuleId;
    }

    /**
     * Return the execution id, for example: 'e4'.
     */
    private String getFlowExecutionId(RequestContext context) {
        String executionValue = context.getRequestParameters().get("execution");
        return executionValue.substring(0, executionValue.lastIndexOf('s'));
    }

    private class FlowMenuEntry implements Serializable {
        static final private long serialVersionUID = 1L;

        private String flowId;
        private String flowExecutionId;

        private String label; // may be null
        private String flowExecutionUrl;
        private Serializable entityId;

        public FlowMenuEntry(String flowId, String flowExecutionId) {
            this.flowId = flowId;
            this.flowExecutionId = flowExecutionId;
        }

        public String getFlowId() {
            return flowId;
        }

        public String getFlowExecutionId() {
            return flowExecutionId;
        }

        public String getLabel() {
            return label;
        }

        public void setLabel(String label) {
            this.label = label;
        }

        public boolean isLabelSet() {
            return label != null && label.length() > 0;
        }

        public String getFlowExecutionUrl() {
            return flowExecutionUrl;
        }

        public void setFlowExecutionUrl(String flowExecutionUrl) {
            this.flowExecutionUrl = flowExecutionUrl;
        }

        public void setEntityId(Serializable entityId) {
            this.entityId = entityId;
        }

        public Serializable getEntityId() {
            return entityId;
        }

        @Override
        public String toString() {
            return flowId + ":" + flowExecutionId + ":" + label + ":" + flowExecutionUrl;
        }
    }

    private boolean maxExecutionsExceeded() {
        return maxExecutions > 0 && flowMenuEntries.size() > maxExecutions;
    }

    private FlowMenuEntry getFlowMenuEntry(String flowId, String flowExecutionId) {
        for (FlowMenuEntry fme : flowMenuEntries) {
            if (fme.getFlowId().equals(flowId) && fme.getFlowExecutionId().equals(flowExecutionId)) {
                return fme;
            }
        }
        return null;
    }

    public List<FlowMenuEntry> getFlowMenuEntries() {
        List<FlowMenuEntry> result = newArrayList();
        for (FlowMenuEntry fme : flowMenuEntries) {
            if (fme.isLabelSet()) {
                result.add(fme);
            }
        }

        return result;
    }

    public List<FlowMenuEntry> getFlowMenuEntries(String flowId) {
        List<FlowMenuEntry> result = newArrayList();
        for (FlowMenuEntry fme : flowMenuEntries) {
            if (fme.getFlowId().equals(flowId) && fme.isLabelSet()) {
                result.add(fme);
            }
        }

        return result;
    }
}