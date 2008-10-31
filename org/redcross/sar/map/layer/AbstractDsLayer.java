package org.redcross.sar.map.layer;

import java.io.IOException;

import org.redcross.sar.ds.DsPool;
import org.redcross.sar.ds.IDs;
import org.redcross.sar.ds.IDsObject;
import org.redcross.sar.ds.event.IDsChangeListener;
import org.redcross.sar.ds.event.IDsPoolListener;
import org.redcross.sar.ds.event.DsEvent.Execute;
import org.redcross.sar.ds.event.DsEvent.Install;
import org.redcross.sar.ds.event.DsEvent.Update;

import com.esri.arcgis.geometry.IEnvelope;
import com.esri.arcgis.geometry.ISpatialReference;
import com.esri.arcgis.interop.AutomationException;

public abstract class AbstractDsLayer<T extends IDsObject> extends AbstractDiskoLayer {

	private static final long serialVersionUID = 1L;

	protected final DsPool pool;
	protected final Class<T> dataClass;

	protected IDs<T> source;

	/* =======================================================
	 * Constructor
	 * ======================================================= */

	public AbstractDsLayer(Class<T> dataClass, String name, LayerCode layerCode,
			ISpatialReference srs) throws Exception {

		// forward
		super(name,layerCode,srs);

		// prepare
		this.dataClass = dataClass;
		this.pool = DsPool.getInstance();

		// add install listener
		pool.addPoolListener(new DsAdapter());

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

	private class DsAdapter implements IDsChangeListener, IDsPoolListener {

		public void handleExecuteEvent(Execute e) { /* NOP */ }

		@SuppressWarnings("unchecked")
		public void handleInstallEvent(Install e) {
			// supported class?
			if(e.getSource().isSupported(dataClass)) {
				// install?
				if(e.getFlags()==0 && source==null) {
					// install
					source = (IDs<T>)e.getSource();
					source.addChangeListener(this);
					// clear current object
					clear();
					// load objects
					for(T it : source.getItems())
						add(it);

				}
				// uninstall?
				else if(e.getFlags()==1 && source!=null) {
					// uninstall
					source.removeChangeListener(this);
					source = null;
					// clear current object
					clear();
				}
			}
		}

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


	}


}