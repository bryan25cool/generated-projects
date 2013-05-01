/*
 * (c) Copyright 2005-2013 JAXIO, http://www.jaxio.com
 * Source code generated by Celerio, a Jaxio product
 * Want to use Celerio within your company? email us at info@jaxio.com
 * Follow us on twitter: @springfuse
 * Template pack-selenium-primefaces:src/test/java/selenium/pages/entity/EditPage.e.vm.java
 */
package com.jaxio.web.selenium.page.role;

import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import com.jaxio.web.selenium.page.AbstractEditPage;
import com.jaxio.web.selenium.support.Page;

@Page
public class RoleEditPage extends AbstractEditPage {
    // edit box
    @FindBy(id = "form:roleName")
    public WebElement roleName;

}