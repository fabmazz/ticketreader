package org.dslul.ticketreader;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.nfc.tech.IsoDep;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;

import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TextView;

import java.util.Calendar;
import java.util.List;
import java.util.concurrent.TimeUnit;

import android.nfc.NfcAdapter;
import android.nfc.tech.NfcA;

import android.widget.Toast;

import android.content.Intent;
import android.content.IntentFilter;

import android.app.PendingIntent;

import android.os.Handler;
import android.os.Message;

import android.app.AlertDialog;

import android.content.DialogInterface;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.yarolegovich.lovelydialog.LovelyCustomDialog;
import com.yarolegovich.lovelydialog.LovelyInfoDialog;
import com.yarolegovich.lovelydialog.LovelyStandardDialog;

import org.dslul.ticketreader.util.HelperFunctions;


public class MainActivity extends AppCompatActivity {

    private NfcAdapter mNfcAdapter;
	private IntentFilter tech;
	private IntentFilter[] intentFiltersArray;
	private PendingIntent pendingIntent;
	private Intent intent;
	private AlertDialog alertDialog;

	private Toast currentToast;

    private AdView adview;
    private ImageView imageNfc;
    private CardView ticketCard;
    private CardView statusCard;
    private ImageView statusImg;
    private TextView statoBiglietto;
    private TextView infoLabel;
	private TableLayout infoTable;
	private TextView tipologia;
    private TextView dataLabel;
    private TextView dataObliterazione;
	private TextView corseRimanenti;

	private CountDownTimer timer;

	private List<byte[]> dump;

    // list of NFC technologies detected:
	private final String[][] techListsArray = new String[][] {
            new String[] {
                //MifareUltralight.class.getName(),
                NfcA.class.getName()
            },
            new String[] {
                IsoDep.class.getName()
            }
	};


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        adview = (AdView) findViewById(R.id.adView);
        imageNfc = (ImageView) findViewById(R.id.imagenfcView);
        ticketCard = (CardView) findViewById(R.id.ticketCardView);
        statusCard = (CardView) findViewById(R.id.statusCardView);
        statusImg = (ImageView) findViewById(R.id.statusImg);
        statoBiglietto = (TextView) findViewById(R.id.stato_biglietto);
        infoLabel = (TextView) findViewById(R.id.infolabel);
        infoTable = (TableLayout) findViewById(R.id.info_table);
		tipologia = (TextView) findViewById(R.id.tipologia);
        dataLabel = (TextView) findViewById(R.id.validation_or_expire);
        dataObliterazione = (TextView) findViewById(R.id.data_obliterazione);
        corseRimanenti = (TextView) findViewById(R.id.corse_rimaste);

        MobileAds.initialize(this, "ca-app-pub-2102716674867426~1964394961");
        AdRequest adRequest = new AdRequest.Builder().build();
        adview.loadAd(adRequest);

        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
		if (mNfcAdapter == null) {
			Toast.makeText(this, R.string.nfc_not_supported, Toast.LENGTH_LONG).show();
			finish();
			return;
		}

		if (!mNfcAdapter.isEnabled()) {
			Toast.makeText(this, R.string.nfc_disabled, Toast.LENGTH_LONG).show();
	        startActivity(new Intent(android.provider.Settings.ACTION_WIRELESS_SETTINGS));
		}

		tech = new IntentFilter(NfcAdapter.ACTION_TECH_DISCOVERED);
		intentFiltersArray = new IntentFilter[] {tech};
		intent = new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
		//FLAG_ACTIVITY_REORDER_TO_FRONT FLAG_RECEIVER_REPLACE_PENDING
		pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);

        onNewIntent(getIntent());
/*
        new LovelyInfoDialog(this)
                .setTopColorRes(R.color.darkBlueGrey)
                .setIcon(R.drawable.ic_info_outline_white_36dp)
                //This will add Don't show again checkbox to the dialog. You can pass any ID as argument
                .setNotShowAgainOptionEnabled(0)
                .setNotShowAgainOptionChecked(false)
                .setTitle("BETA")
                .setMessage("Questa versione è una beta e potrebbe restituire risultati sbagliati. Segnalare per favore ogni incongruenza (insieme ad una copia del contenuto della carta) all'indirizzo email specificato nelle informazioni. Grazie.")
                .show();
*/
    }

    @Override
	protected void onResume() {
		super.onResume();
		mNfcAdapter.enableForegroundDispatch(this, pendingIntent, intentFiltersArray, this.techListsArray);
	}

	@Override
	protected void onPause() {
		// disabling foreground dispatch:
		//NfcAdapter nfcAdapter = NfcAdapter.getDefaultAdapter(this);
		mNfcAdapter.disableForegroundDispatch(this);
		super.onPause();
	}

    @Override
    protected void onNewIntent(Intent intent) {
		if (intent.getAction().equals(NfcAdapter.ACTION_TECH_DISCOVERED)) {

			NfcThread nfcThread = new NfcThread(getBaseContext(), intent, mContentHandler, mToastShortHandler, mToastLongHandler, mShowInfoDialogHandler);
			nfcThread.start();
		}
    }

	@SuppressLint("HandlerLeak")
	private Handler mContentHandler = new Handler() {
		public void handleMessage(Message msg) {
			List dumplist = (List<byte[]>)msg.obj;
            dump = dumplist;

			if(timer != null)
				timer.cancel();

			try {
                //smartcard
                if(dumplist.size() == 15) {
                    SmartCard smartcard = new SmartCard(dumplist);
                    if(smartcard.hasSubscriptions() && !smartcard.hasTickets()) {
                        dataLabel.setText(R.string.expire_date);
                        tipologia.setText(smartcard.getSubscriptionName());
                        dataObliterazione.setText(smartcard.getExpireDate());

                        if(smartcard.isSubscriptionExpired()) {
                            corseRimanenti.setText("0");
                            statoBiglietto.setText(R.string.expired);
                            statusImg.setImageResource(R.drawable.ic_error_grey_800_36dp);
                            statusCard.setCardBackgroundColor(getResources().getColor(R.color.colorRed));
                        } else {
                            corseRimanenti.setText(R.string.unlimited);
                            statoBiglietto.setText(R.string.valid);
                            statusImg.setImageResource(R.drawable.ic_check_circle_grey_800_36dp);
                            statusCard.setCardBackgroundColor(getResources().getColor(R.color.colorGreen));
                        }

                        statusCard.setVisibility(View.VISIBLE);
                        ticketCard.setVisibility(View.VISIBLE);
                        infoLabel.setText(R.string.read_another_ticket);
                        imageNfc.setVisibility(View.GONE);
                    } else {

                        createTicketInterface(smartcard.getTicketName(),smartcard.getValidationDate(),
                                smartcard.getRemainingRides(), smartcard.getRemainingMinutes());
                        //Toast.makeText(getBaseContext(), R.string.smartcard_tickets_not_supported_yet, Toast.LENGTH_LONG).show();

                    }


                }
                //chip on paper
                else if(dumplist.size() > 15) {
                    ChipOnPaper chipOnPaper = new ChipOnPaper(dumplist);
                    createTicketInterface(chipOnPaper.getTypeName(),chipOnPaper.getDate(),
                                    chipOnPaper.getRemainingRides(), chipOnPaper.getRemainingMinutes());


                } else {
                    statusCard.setVisibility(View.GONE);
                    ticketCard.setVisibility(View.GONE);
                    infoLabel.setText(R.string.info_instructions);
                    imageNfc.setVisibility(View.VISIBLE);
                }

            } catch (Exception ex) {
                Toast.makeText(getBaseContext(), R.string.unknown_error, Toast.LENGTH_LONG).show();
                Log.d("card", ex.getMessage());
				ex.printStackTrace();
            }

        }
	};



    private void createTicketInterface(String name, String date, int remainingRides, long remainingMinutes) {
        dataLabel.setText(R.string.data_obliterazione);
		tipologia.setText(name);
		dataObliterazione.setText(date);
		corseRimanenti.setText(Integer.toString(remainingRides));

		if(remainingMinutes != 0) {
			statoBiglietto.setText(R.string.in_corso);
			statusImg.setImageResource(R.drawable.ic_restore_grey_800_36dp);
			statusCard.setCardBackgroundColor(getResources().getColor(R.color.colorBlue));
			Calendar calendar = Calendar.getInstance();
			int sec = calendar.get(Calendar.SECOND);
			timer = new CountDownTimer((remainingMinutes*60 - sec)*1000, 1000) {

				public void onTick(long millis) {
					statoBiglietto.setText(String.format(getResources().getString(R.string.in_corso),
							TimeUnit.MILLISECONDS.toMinutes(millis),
							TimeUnit.MILLISECONDS.toSeconds(millis) -
									TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis))));
				}

				public void onFinish() {
					statoBiglietto.setText(R.string.corse_esaurite);
					statusImg.setImageResource(R.drawable.ic_error_grey_800_36dp);
					statusCard.setCardBackgroundColor(getResources().getColor(R.color.colorRed));
					if(timer != null)
						timer.cancel();
				}

			}.start();
		} else if(remainingRides == 0 && remainingMinutes == 0) {
			statoBiglietto.setText(R.string.corse_esaurite);
			statusImg.setImageResource(R.drawable.ic_error_grey_800_36dp);
			statusCard.setCardBackgroundColor(getResources().getColor(R.color.colorRed));
		} else if(remainingRides != 0 && remainingMinutes == 0) {
			statoBiglietto.setText(String.format(getResources().getString(R.string.corse_disponibili), remainingRides));
			statusImg.setImageResource(R.drawable.ic_check_circle_grey_800_36dp);
			statusCard.setCardBackgroundColor(getResources().getColor(R.color.colorGreen));
		}

		statusCard.setVisibility(View.VISIBLE);
		ticketCard.setVisibility(View.VISIBLE);
		infoLabel.setText(R.string.read_another_ticket);
		imageNfc.setVisibility(View.GONE);
	}







	private Handler mToastShortHandler = new Handler() {
		public void handleMessage(Message msg) {
			String text = (String)msg.obj;
            if(currentToast != null)
			    currentToast.cancel();
			currentToast = Toast.makeText(MainActivity.this, text, Toast.LENGTH_SHORT);
			currentToast.show();
		}
	};

	private Handler mToastLongHandler = new Handler() {
		public void handleMessage(Message msg) {
			String text = (String)msg.obj;
			if(currentToast != null)
			    currentToast.cancel();
			currentToast = Toast.makeText(MainActivity.this, text, Toast.LENGTH_LONG);
			currentToast.show();
		}
	};

	private Handler mShowInfoDialogHandler = new Handler() {
		public void handleMessage(Message msg) {
			String text = (String)msg.obj;
			//infoDialog = showInfoDialog(text);
			//infoDialog.show();
		}
	};



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_info) {
            TextView view = new TextView(getBaseContext());
            view.setText(Html.fromHtml(getString(R.string.html_info)));
            view.setMovementMethod(LinkMovementMethod.getInstance());
            view.setPadding( 40, 40, 40, 40 );
            new LovelyCustomDialog(this)
                    .setTopColorRes(R.color.darkBlueGrey)
                    .setIcon(R.drawable.ic_info_outline_white_36dp)
                    .setTitle("Info")
                    .setView(view)
                    .show();
            return true;
        }

        if (id == R.id.action_copy_content) {
            try {
                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                String content = "";
                if (dump != null && dump.size() >= 15) {
                    for (byte[] page : dump) {
                        content = content.concat(HelperFunctions.byteArrayToHexString(page));
                    }
                    ClipData clip = ClipData.newPlainText("content", content);
                    if (clipboard != null) {
                        clipboard.setPrimaryClip(clip);
                    }
                    Toast.makeText(getBaseContext(), R.string.content_copied, Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(getBaseContext(), R.string.no_content, Toast.LENGTH_LONG).show();
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }

            return true;
        }

        return super.onOptionsItemSelected(item);
    }


	private AlertDialog showAlertDialog(String message) {
		DialogInterface.OnClickListener dialogInterfaceListener = new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				alertDialog.cancel();
			}
		};

		alertDialog = new AlertDialog.Builder(this)
				.setTitle(R.string.information)
				.setIcon(android.R.drawable.ic_dialog_info)
				.setMessage(message)
   				.setPositiveButton(R.string.close_dialog, null)
				.create();

		alertDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {

			public void onCancel(DialogInterface dialog) {
			}
		});

		return alertDialog;
	}

}
