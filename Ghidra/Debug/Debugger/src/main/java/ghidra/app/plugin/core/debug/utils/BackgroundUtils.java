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
package ghidra.app.plugin.core.debug.utils;

import java.util.List;
import java.util.concurrent.*;
import java.util.function.BiFunction;

import org.apache.commons.lang3.exception.ExceptionUtils;

import ghidra.framework.cmd.BackgroundCommand;
import ghidra.framework.model.DomainObject;
import ghidra.framework.model.UndoableDomainObject;
import ghidra.framework.plugintool.PluginTool;
import ghidra.util.exception.CancelledException;
import ghidra.util.task.*;

public enum BackgroundUtils {
	;

	public static class AsyncBackgroundCommand<T extends UndoableDomainObject>
			extends BackgroundCommand {
		private CompletableFuture<?> promise;

		private final CancelledListener cancelledListener = this::cancelled;
		private final BiFunction<T, TaskMonitor, CompletableFuture<?>> futureProducer;

		private AsyncBackgroundCommand(String name, boolean hasProgress, boolean canCancel,
				boolean isModal, BiFunction<T, TaskMonitor, CompletableFuture<?>> futureProducer) {
			super(name, hasProgress, canCancel, isModal);
			this.futureProducer = futureProducer;
		}

		private void cancelled() {
			promise.cancel(true);
		}

		@Override
		@SuppressWarnings("unchecked")
		public boolean applyTo(DomainObject obj, TaskMonitor monitor) {
			promise = futureProducer.apply((T) obj, monitor);
			monitor.addCancelledListener(cancelledListener);
			try {
				promise.get();
				return true;
			}
			catch (InterruptedException e) {
				setStatusMsg("Interrupted");
				return false;
			}
			catch (ExecutionException e) {
				setStatusMsg(e.getMessage());
				if (!(e.getCause() instanceof CancellationException)) {
					return ExceptionUtils.rethrow(e.getCause());
				}
				return false;
			}
			catch (CancellationException e) {
				setStatusMsg("Cancelled");
				return false;
			}
		}
	}

	public static <T extends UndoableDomainObject> AsyncBackgroundCommand<T> async(PluginTool tool,
			T obj, String name, boolean hasProgress, boolean canCancel, boolean isModal,
			BiFunction<T, TaskMonitor, CompletableFuture<?>> futureProducer) {
		AsyncBackgroundCommand<T> cmd =
			new AsyncBackgroundCommand<>(name, hasProgress, canCancel, isModal, futureProducer);
		tool.executeBackgroundCommand(cmd, obj);
		return cmd;
	}

	public static class PluginToolExecutorService extends AbstractExecutorService {
		private final PluginTool tool;
		private String name;
		private boolean canCancel;
		private boolean hasProgress;
		private boolean isModal;
		private final int delay;

		public PluginToolExecutorService(PluginTool tool, String name, boolean canCancel,
				boolean hasProgress, boolean isModal, int delay) {
			this.tool = tool;
			this.name = name;
			this.canCancel = canCancel;
			this.hasProgress = hasProgress;
			this.isModal = isModal;
			this.delay = delay;
		}

		@Override
		public void shutdown() {
			throw new UnsupportedOperationException();
		}

		@Override
		public List<Runnable> shutdownNow() {
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean isShutdown() {
			return false;
		}

		@Override
		public boolean isTerminated() {
			return false;
		}

		@Override
		public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
			throw new UnsupportedOperationException();
		}

		@Override
		public void execute(Runnable command) {
			Task task = new Task(name, canCancel, hasProgress, isModal) {
				@Override
				public void run(TaskMonitor monitor) throws CancelledException {
					command.run();
				}
			};
			tool.execute(task, delay);
		}
	}
}
