/*
 * Copyright 2001-2004 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package gov.epa.emissions.framework.service;

import java.util.ArrayList;
import java.util.List;

public class BeanService
{
    public List getList(){
       
        ArrayList list = new ArrayList();
        
        Order order = new Order();
        order.setCustomerName("Fred McNary");
        order.setShippingAddress("200 Sylvan Learning Drive, Boston, MA");
        
        String [] items = new String[] { "MI53jukebox", "999mahBattery" };
        int [] quantities = new int [] { 99, 99 };
        
        order.setItemCodes(items);
        order.setQuantities(quantities);
        
        return list;
    }
    
    public String processOrder(Order order)
    {
        String sep = System.getProperty("line.separator");
        
        String response = "Hi, " + order.getCustomerName() + "!" + sep;
        
        response += sep + "You seem to have ordered the following:" + sep;
        
        String [] items = order.getItemCodes();
        int [] quantities = order.getQuantities();
        
        for (int i = 0; i < items.length; i++) {
            response += sep + quantities[i] + " of item : " + items[i];
        }
        
        response += sep + sep +
                    "If this had been a real order processing system, "+
                    "we'd probably have charged you about now.";
        
        return response;
    }
}
