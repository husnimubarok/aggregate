/*
 * Copyright (C) 2011 University of Washington
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package org.opendatakit.aggregate.client.widgets;

import java.util.List;

import org.opendatakit.aggregate.client.AggregateUI;
import org.opendatakit.aggregate.client.FilterSubTab;
import org.opendatakit.aggregate.client.SecureGWT;
import org.opendatakit.aggregate.client.filter.Filter;
import org.opendatakit.aggregate.client.filter.FilterGroup;
import org.opendatakit.aggregate.constants.common.UIConsts;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.ListBox;

public class SaveFilterGroupButton extends AButtonBase implements ClickHandler {

  private static final String DEFAULT_SUGGESTION = "FilterGroup";
  private static final String ERROR_NO_FILTERS = "You need at least one filter to save a group.";
  private static final String PROMPT_FOR_NAME_TXT = "Please enter a name for this group";
  private static final String REPROMPT_FOR_NAME_TXT = "That group already exists. Please enter a new name";

  private FilterSubTab parentSubTab;

  public SaveFilterGroupButton(FilterSubTab parentSubTab) {
    super("Save");
    this.parentSubTab = parentSubTab;
    addStyleDependentName("positive");
    addClickHandler(this);
  }

  @Override
  public void onClick(ClickEvent event) {
    super.onClick(event);

    FilterGroup filterGroup = parentSubTab.getDisplayedFilterGroup();
    List<Filter> filters = filterGroup.getFilters();

    if (filters == null || filters.size() <= 0) {
      Window.alert(ERROR_NO_FILTERS);
      return;
    }

    // TODO insert a cancel so that this while loop can stop
    if (UIConsts.FILTER_NONE.equals(filterGroup.getName())) {
      boolean match = false;
      String newFilterName = Window.prompt(PROMPT_FOR_NAME_TXT, DEFAULT_SUGGESTION);
      while (true) {
        ListBox filtersBox = parentSubTab.getListOfPossibleFilterGroups();
        if (newFilterName != null) {
          for (int i = 0; i < filtersBox.getItemCount(); i++) {
            if ((filtersBox.getValue(i)).compareTo(newFilterName) == 0) {
              match = true;
            }
          }
        }
        if (match) {
          match = false;
          newFilterName = Window.prompt(REPROMPT_FOR_NAME_TXT, DEFAULT_SUGGESTION);
        } else {
          break;
        }
      }
      filterGroup.setName(newFilterName);
    }

    // Set up the callback object.
    AsyncCallback<Boolean> callback = new AsyncCallback<Boolean>() {
      public void onFailure(Throwable caught) {
        AggregateUI.getUI().reportError(caught);
      }

      @Override
      public void onSuccess(Boolean result) {
        parentSubTab.update();
      }
    };
    
    // Make the call to the form service.
    SecureGWT.getFilterService().updateFilterGroup(filterGroup, callback);

  }
}