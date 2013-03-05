package org.redcross.sar.map.layer;

import org.apache.log4j.Logger;
import org.redcross.sar.data.IData;
import org.redcross.sar.ds.DsPool;
import org.redcross.sar.ds.IDs;
import org.redcross.sar.ds.IDsObject;
import org.redcross.sar.ds.event.IDsChangeListener;
import org.redcross.sar.ds.event.DsEvent.Update;
import org.redcross.sar.event.ICatalogListener;
import org.redcross.sar.event.CatalogEvent.Instance;
import org.redcross.sar.map.event.MapLayerEventStack;

public abstract class AbstractDsObjectLayer<I,E,D extends IDsObject,G extends IData> 
	extends AbstractMapLayer<I,E,D,G> 
	implements IDsObjectLayer<I,E,D,G> {

	private static final long serialVersionUID = 1L;

	protected final DsPool pool;
	protected final Class<? extends IDs<?>> sourceClass;

	protected IDs<D> source;
	
	protected static Logger logger = Logger.getLogger(AbstractDsObjectLayer.class);

	/* =======================================================
	 * Constructor
	 * ======================================================= */

	public AbstractDsObjectLayer(Class<? extends IDs<?>> sourceClass, 
			String name, Enum<?> classCode, LayerCode layerCode,
			MapLayerEventStack eventStack) throws Exception {

		// forward
		super(name,classCode,layerCode,eventStack);

		// prepare
		this.sourceClass = sourceClass;
		this.pool = DsPool.getInstance();

		// add install listener
		pool.addCatalogListener(new DsAdapter());

	}

	/* =======================================================
	 * IDsObjectLayer implementation
	 * ======================================================= */
	
	public LayerCode getLayerCode() {
		return (LayerCode)layerCode;
	}
	
	public IDs<D> getSource() {
		return source;
	}

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
				switch(e.getType()) {
				case ADDED_EVENT:
					data = e.getData();
					for(int i=0;i<data.length;i++) {
						setDirtyExtent(addDataObject((D)data[i]));
					}
					break;
				case MODIFIED_EVENT:
					data = e.getData();
					for(int i=0;i<data.length;i++) {
						setDirtyExtent(updateDataObject((D)data[i]));
					}

					break;
				case REMOVED_EVENT:
					data = e.getData();
					for(int i=0;i<data.length;i++) {
						setDirtyExtent(removeDataObject((D)data[i]));
					}
					break;
				}
				// forward
				refresh();

			} catch (Exception ex) {
				logger.error("Failed to handle update event",ex);
			}
		}

		/* =======================================================
		 * Helper methods
		 * ======================================================= */

		private void connect(IDs<D> source) {
			// forward
			disconnect();
			// connect?
			if(source!=null) {
				// connect
				AbstractDsObjectLayer.this.source = source;
				AbstractDsObjectLayer.this.source.addChangeListener(this);
				// load objects
				loadDataObjects(source.getItems());
			}
		}

		private void disconnect() {
			// disconnect?
			if(source!=null) {
				// disconnect
				source.removeChangeListener(this);
				source = null;
				// clear current object
				clearDataObjects();
			}
		}

	}


}