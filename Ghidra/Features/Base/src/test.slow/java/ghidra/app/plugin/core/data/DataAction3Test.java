/* ###
 * IP: GHIDRA
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
package ghidra.app.plugin.core.data;

import java.util.List;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import generic.test.category.NightlyCategory;
import ghidra.program.model.data.DataType;

@Category(NightlyCategory.class)
public class DataAction3Test extends AbstractDataActionTest {

	@Test
	public void testAllDefaultDataSettings() throws Exception {

		List<DataType> builtIns = getBuiltInDataTypesAsFavorites();
		for (DataType type : builtIns) {
			String actionName = "Define " + type.getName();
			manipulateAllSettings(true, false, false, getAction(actionName));
		}
	}

	@Test
	public void testAllDataSettings() throws Exception {

		List<DataType> builtIns = getBuiltInDataTypesAsFavorites();
		for (DataType type : builtIns) {
			String actionName = "Define " + type.getName();
			manipulateAllSettings(false, false, false, getAction(actionName));
		}
	}
}
