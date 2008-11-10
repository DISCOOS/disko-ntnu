package org.redcross.sar.map.layer;

import java.io.IOException;

import org.redcross.sar.app.event.ICatalogListener;
import org.redcross.sar.app.event.CatalogEvent.Instance;
import org.redcross.sar.ds.DsPool;
import org.redcross.sar.ds.IDs;
import org.redcross.sar.ds.IDsObject;
import org.redcross.sar.ds.event.IDsChangeListener;
import org.redcross.sar.ds.event.DsEvent.Update;

import com.esri.arcgis.geometry.IEnvelope;
import com.esri.arcgis.geometry.ISpatialReference;
import com.esri.arcgis.interop.AutomationException;

public abstract class AbstractDsLayer<T extends IDsObject> extends AbstractDiskoLayer {

	private static final long serialVersionUID = 1L;

	protected final DsPool pool;
	protected final Class<? extends IDs<?>> sourceClass;

	protected IDs<T> source;

	/* =======================================================
	 * Constructor
	 * ======================================================= */

	public AbstractDsLayer(Class<? extends IDs<?>> sourceClass, String name, LayerCode layerCode,
			ISpatialReference srs) throws Exception {

		// forward
		super(name,layerCode,srs);

		// prepare
		this.sourceClass = sourceClass;
		this.pool = DsPool.getInstance();

		// add install listener
		pool.addCatalogListener(new DsAdapter());

	}

	/* =======================================================
	 * Required methods
	 * ======================================================= */

	protected abstract IEnvelope add(T data);
	protected abstract IEnvelope update(T data);
	protected abstract IEnvelope remove(T data);
	protected abstract void clear();

	/* =======================================================
	 * Inner classes
	 * ======================================================= */

	private class DsAdapter implements IDsChangeListener, ICatalogListener {

		/* ==============================================
		 * ICatalogListener implementation
		 * ============================================== */

		@Override
		public void handleCatalogEvent(Instance e) {
			// supported source class?
			if(sourceClass.isAssignableFrom(e.getSource().getClass())) {
				// connect?
				if(e.getFlags()==0) {
					// forward
					connect(source);
				}
				// disconnect?
				else if(e.getFlags()==1) {
					// forward
					disconnect();
				}
			}
		}

		/* ==============================================
		 * IDsChangeListener implementation
		 * ============================================== */

		@SuppressWarnings("unchecked")
		public void handleUpdateEvent(Update e) {
			try {
				Object[] data;
				IEnvelope extent = null;
				switch(e.getType()) {
				case ADDED_EVENT:
					data = e.getData();
					for(int i=0;i<data.length;i++) {
						IEnvelope added = add((T)data[i]);
						if(added!=null && !added.isEmpty()) {
							if(extent==null)
								extent = added;
							else
								extent.union(added);
						}
					}
					break;
				case MODIFIED_EVENT:
					data = e.getData();
					for(int i=0;i<data.length;i++) {
						IEnvelope updated = update((T)data[i]);
						if(updated!=null && !updated.isEmpty()) {
							if(extent==null)
								extent = updated;
							else
								extent.union(updated);
						}
					}

					break;
				case REMOVED_EVENT:
					data = e.getData();
					for(int i=0;i<data.length;i++) {
						IEnvelope removed = remove((T)data[i]);
						if(removed!=null && !removed.isEmpty()) {
							if(extent==null)
								extent = removed;
							else
								extent.union(removed);
						}
					}
					break;
				}
				// forward
				refresh(extent);

			} catch (AutomationException ex) {
				// TODO Auto-generated catch block
				ex.printStackTrace();
			} catch (IOException ex) {
				// TODO Auto-generated catch block
				ex.printStackTrace();
			}
		}

		/* =======================================================
		 * Helper methods
		 * ======================================================= */

		private void connect(IDs<T> source) {
			// forward
			disconnect();
			// connect?
			if(source!=null) {
				// connect
				AbstractDsLayer.this.source = source;
				AbstractDsLayer.this.source.addChangeListener(this);
				// load objects
				for(T it : source.getItems())
					add(it);
			}
		}

		private void disconnect() {
			// disconnect?
			if(source!=null) {
				// disconnect
				source.removeChangeListener(this);
				source = null;
				// clear current object
				clear();
			}
		}

	}


}