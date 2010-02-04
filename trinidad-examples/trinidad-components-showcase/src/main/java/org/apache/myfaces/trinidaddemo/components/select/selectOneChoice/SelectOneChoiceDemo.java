/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.myfaces.trinidaddemo.components.select.selectOneChoice;

import org.apache.myfaces.trinidaddemo.support.impl.AbstractComponentDemo;
import org.apache.myfaces.trinidaddemo.support.impl.ComponentVariantDemoImpl;
import org.apache.myfaces.trinidaddemo.support.ComponentDemoId;
import org.apache.myfaces.trinidaddemo.support.IComponentDemoVariantId;

/**
 *
 */
public class SelectOneChoiceDemo extends AbstractComponentDemo {

    private static final long serialVersionUID = -1989061956014398510L;

    private enum VARIANTS implements IComponentDemoVariantId {
		Simple,
        Detailed
	}

	/**
	 * Constructor.
	 */
	public SelectOneChoiceDemo() {
		super(ComponentDemoId.selectOneChoice, "Select One Choice");

        addComponentDemoVariant(new ComponentVariantDemoImpl(VARIANTS.Simple, this,
                "/components/select/selectOneChoice/selectOneChoiceSimple.xhtml", getSummaryResourcePath()));
        addComponentDemoVariant(new ComponentVariantDemoImpl(VARIANTS.Detailed, this,
                "/components/select/selectOneChoice/selectOneChoiceDetailed.xhtml", getSummaryResourcePath()));
	}

	public String getJsfResourcePath() {
		return "/components/select/selectOneChoice/selectOneChoice.xhtml";
	}

    public String getSummaryResourcePath() {
        return "/components/select/selectOneChoice/summary.xhtml";
    }
}