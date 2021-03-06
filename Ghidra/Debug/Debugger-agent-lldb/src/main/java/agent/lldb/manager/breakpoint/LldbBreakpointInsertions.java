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
package agent.lldb.manager.breakpoint;

import java.util.concurrent.CompletableFuture;

import ghidra.util.NumericUtilities;

public interface LldbBreakpointInsertions {
	/**
	 * Insert a breakpoint
	 * 
	 * This is equivalent to the CLI command: {@code break [LOC]}, or {@code watch [LOC]}, etc.
	 * 
	 * Breakpoints in lldb can get pretty complicated. Depending on the location specification, the
	 * actual location of the breakpoint may change during the lifetime of an inferior. Take note of
	 * the breakpoint number to track those changes across breakpoint modification events.
	 * 
	 * @param loc the location (address, symbol, line number, etc.) to place the breakpoint at
	 * @param type the type of breakpoint (or watchpoint) to add
	 * @return a copy of the resulting breakpoint information once lldb has executed the command
	 */
	default CompletableFuture<LldbBreakpointInfo> insertBreakpoint(String loc,
			LldbBreakpointType type) {
		return insertBreakpoint(NumericUtilities.parseHexLong(loc), 1,
			LldbBreakpointType.BREAKPOINT);
	}

	/**
	 * Insert a (usually software) execution breakpoint
	 * 
	 * @param loc string version of address
	 * @return a future that completes when lldb has executed the command
	 * 
	 * @see #insertBreakpoint(String location)
	 */
	default CompletableFuture<LldbBreakpointInfo> insertBreakpoint(String loc) {
		return insertBreakpoint(loc, LldbBreakpointType.BREAKPOINT);
	}

	/**
	 * Insert a (usually software) execution breakpoint at the given address offset
	 * 
	 * Note, this uses the "Address Notation" specified by lldb.
	 * 
	 * @param addr breakpoint address
	 * @return a future that completes when lldb has executed the command
	 * 
	 * @see #insertBreakpoint(long address)
	 */
	default CompletableFuture<LldbBreakpointInfo> insertBreakpoint(long addr) {
		return insertBreakpoint(addr, 1, LldbBreakpointType.BREAKPOINT);
	}

	/**
	 * Insert a breakpoint (usually a watchpoint) at the given address range
	 * 
	 * Note, this implements the length by casting the address pointer to a
	 * fixed-length-char-array-pointer where the array has the given length. Support for specific
	 * lengths may vary by platform.
	 * 
	 * @param addr the starting address
	 * @param len the length of the range
	 * @param type the type of breakpoint (usually a watchpoint)
	 * @return a future that completes when lldb has executed the command
	 * @see #insertBreakpoint(String, LldbBreakpointType)
	 */
	CompletableFuture<LldbBreakpointInfo> insertBreakpoint(long addr, int len,
			LldbBreakpointType type);

}
