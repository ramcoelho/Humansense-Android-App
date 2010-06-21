package ca.mcgill.hs.plugin;

import java.util.List;


import android.content.Context;
import android.telephony.CellLocation;
import android.telephony.NeighboringCellInfo;
import android.telephony.PhoneStateListener;
import android.telephony.ServiceState;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.telephony.gsm.GsmCellLocation;
import android.util.Log;

public class GSMLogger extends InputPlugin{
	private static final String TAG = "GSMLogger";
	private final TelephonyManager tm;
	private PhoneStateListener psl;
	private Thread updateThread;
	
	private static long time;
	private static int cid;
	private static int lac;
	private static int ns;
	private static int[] cids;
	private static int[] lacs;
	private static int[] rssis;
	
	public GSMLogger(TelephonyManager tm, Context context) {
		this.tm = tm;
	}	

	/**
	 * Taken from Jordan Frank (hsandroidv1.ca.mcgill.cs.humansense.hsandroid.service) and
	 * modified for this plugin.
	 * 
	 * @Override
	 */
	public void startPlugin() {
		if (tm.getPhoneType() == TelephonyManager.PHONE_TYPE_GSM && tm.getSimState() != TelephonyManager.SIM_STATE_ABSENT) {
			psl = new PhoneStateListener() {
				private int rssi = -1, mcc = -1, mnc = -1;

				@Override
				public void onSignalStrengthsChanged(SignalStrength strength) {
					super.onSignalStrengthsChanged(strength);
					final int asu = strength.getGsmSignalStrength();
					if (asu == -1) {
						rssi = -1;
					} else {
						rssi = (-113 + 2 * asu);
					}
					
					logSignals((GsmCellLocation)tm.getCellLocation());
				}

				@Override
				public void onCellLocationChanged(CellLocation cell) {
					super.onCellLocationChanged(cell);
					logSignals((GsmCellLocation)cell);
				}

				@Override
				public void onServiceStateChanged(ServiceState serviceState) {
					super.onServiceStateChanged(serviceState);
					int state = serviceState.getState();
					switch (state) {
					case ServiceState.STATE_IN_SERVICE:
					case ServiceState.STATE_EMERGENCY_ONLY:
						String op = serviceState.getOperatorNumeric();

						if (op.length() > 3) {
							String mccStr = op.substring(0, 3);
							String mncStr = op.substring(3);

							try {
								mcc = Integer.parseInt(mccStr);
								mnc = Integer.parseInt(mncStr);
							} catch (Exception e) {
							}
						}

						break;
					case ServiceState.STATE_POWER_OFF:
						break;
					case ServiceState.STATE_OUT_OF_SERVICE:
						break;
					}

				}

				public void logSignals(GsmCellLocation cell) {
					time = System.currentTimeMillis();
					cid = cell.getCid();
					lac = cell.getLac();

					final List<NeighboringCellInfo> neighbours = tm.getNeighboringCellInfo();
					
					ns = neighbours.size();
					cids = new int[ns];
					lacs = new int[ns];
					rssis = new int[ns];

					int asu, rssi;
					int i = 0;
					for (NeighboringCellInfo neighbour : neighbours) {
						cids[i] = neighbour.getCid();
						lacs[i] = neighbour.getLac();
						asu = neighbour.getRssi();
						if (asu == -1) {
							rssi = -1;
						} else {
							rssi = (-113 + 2 * asu);
						}
						rssis[i] = rssi;
						i++;
					}
					
					write(new GSMLoggerPacket(time, mcc, mnc, cid, lac, this.rssi, ns, cids, lacs, rssis));
				}
			};
			tm.listen(psl, PhoneStateListener.LISTEN_SERVICE_STATE | 
					PhoneStateListener.LISTEN_CELL_LOCATION | 
					PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);
			updateThread = new Thread() {
				public void run() {
					try {
						while (true) {
							Log.i(TAG, "Requesting Cell Location Update.");
							CellLocation.requestLocationUpdate();
							sleep(5000);
						}
					} catch (InterruptedException e) {
						Log.i(TAG, "Logging thread terminated.");
					}
				}
			};
			updateThread.start();
		} else {
			Log.i(TAG, "GSM Location Logging Unavailable! Wrong phone type or SIM card not present!");
		}
		
	}

	@Override
	public void stopPlugin() {
		if (tm.getSimState() != TelephonyManager.SIM_STATE_ABSENT){
			updateThread.interrupt();
			tm.listen(psl, PhoneStateListener.LISTEN_NONE);
		}
	}
	
	// ***********************************************************************************
	// PUBLIC INNER CLASS -- GSMLoggerPacket
	// ***********************************************************************************
	
	public class GSMLoggerPacket implements DataPacket{
		final long time;
		final int mcc;
		final int mnc;
		final int cid;
		final int lac;
		final int rssi;
		final int neighbors;
		final int[] cids;
		final int[] lacs;
		final int[] rssis;

		public GSMLoggerPacket(long time, int mcc, int mnc, int cid, int lac,
				int rssi, int neighbors, int[] cids, int[] lacs,
				int[] rssis) {
			this.time = time;
			this.mcc = mcc;
			this.mnc = mnc;
			this.cid = cid;
			this.lac = lac;
			this.rssi = rssi;
			this.neighbors = neighbors;
			this.cids = cids;
			this.lacs = lacs;
			this.rssis = rssis;
		}

		@Override
		public String getInputPluginName() {
			return "GSMLogger";
		}
		
		public DataPacket clone(){
			return new GSMLoggerPacket(time, mcc, mnc, cid, lac, rssi, neighbors, cids, lacs, rssis);
		}
		
	}

}