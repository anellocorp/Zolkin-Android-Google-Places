package com.imd.zolkin.activity;

import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.Toast;

import br.com.zolkin.R;

import com.imd.zolkin.adapter.MessagesAdapter;
import com.imd.zolkin.model.ZLMessage;
import com.imd.zolkin.model.ZLMessage.ZLMessageType;
import com.imd.zolkin.services.ZLServiceOperationCompleted;
import com.imd.zolkin.services.ZLServiceResponse;
import com.imd.zolkin.services.ZLServices;
import com.innovattic.font.FontTextView;

public class MessagesActivity extends BaseZolkinMenuActivity {
    FrameLayout btMenu = null;
    FontTextView tvBtRecebidas = null;
    FontTextView tvBtArquivadas = null;
    ListView lvMessages = null;

    List<ZLMessage> received, archived;

    MessagesAdapter adapterRecebidas = null, adapterArquivadas = null;

    boolean showingArchivedMessages;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messages);

        btMenu = (FrameLayout) findViewById(R.id.btMenu);
        tvBtRecebidas = (FontTextView) findViewById(R.id.tvBtRecebidas);
        tvBtArquivadas = (FontTextView) findViewById(R.id.tvBtArquivadas);
        lvMessages = (ListView) findViewById(R.id.lvMessages);

        showingArchivedMessages = false;

        showReceived();

        tvBtRecebidas.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                showReceived();
            }
        });

        tvBtArquivadas.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                showArchivedMessages();
            }
        });


        lvMessages.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int pos, long arg3) {
                ZLMessage m = null;
                if (showingArchivedMessages)
                    m = archived.get(pos);
                else
                    m = received.get(pos);

                final ZLMessage finalM = m;
                final boolean[] answered = {false};
                AsyncTask<Void, Void, Void> asyncTask = new AsyncTask<Void, Void, Void>() {
                    @Override
                    protected Void doInBackground(Void... params) {
                        answered[0] = ZLServices.getInstance().isAnsweredSurvey(finalM);
                        return null;
                    }

                    @Override
                    protected void onPostExecute(Void aVoid) {
                        super.onPostExecute(aVoid);
                        if (finalM.idPesquisa != null) {
                            if(answered[0]) {
                                Toast.makeText(MessagesActivity.this, "Você já respondeu esta pesquisa.", Toast.LENGTH_LONG).show();
                                return;
                            } else {
                                SurveyActivity.message = finalM;
                                startActivity(new Intent(MessagesActivity.this, SurveyActivity.class));
                            }
                        } else {
                            MessageActivity.message = finalM;
                            startActivity(new Intent(MessagesActivity.this, MessageActivity.class));
                        }
                    }
                };
                asyncTask.execute();
            }
        });

        lvMessages.setOnItemLongClickListener(new OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> arg0, View arg1, final int pos, long arg3) {
                ZLMessage msg = null;
                if (showingArchivedMessages)
                    msg = archived.get(pos);
                else
                    msg = received.get(pos);

                final ZLMessage m = msg;

                if (m.status == ZLMessageType.ZLMessageTypeNew || m.status == ZLMessageType.ZLMessageTypeUnread) {
                    // archivar
                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(MessagesActivity.this);
                    // set title
                    alertDialogBuilder
                            .setTitle("Zolkin")
                            .setMessage("O que deseja fazer com a mensagem?")
                            .setCancelable(true).
                            setPositiveButton("Arquivar", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    ZLServices.getInstance().changeMessageStatus(m, ZLMessageType.ZLMessageTypeArchived, true, MessagesActivity.this, new ZLServiceOperationCompleted<Boolean>() {

                                        @Override
                                        public void operationCompleted(ZLServiceResponse<Boolean> response) {
                                            if (response.errorMessage != null) {
                                                Toast.makeText(MessagesActivity.this, response.errorMessage, Toast.LENGTH_LONG).show();
                                            } else {
                                                received.remove(pos);
                                                m.status = ZLMessageType.ZLMessageTypeArchived;
                                                archived.add(m);
                                                adapterRecebidas.notifyDataSetChanged();
                                            }
                                        }
                                    });
                                    dialog.cancel();
                                }
                            }).setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                        }
                    })
                            .create()
                            .show();
                } else if (m.status == ZLMessageType.ZLMessageTypeArchived) {
                    // mover para recebidas
                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(MessagesActivity.this);
                    // set title
                    alertDialogBuilder
                            .setTitle("Zolkin")
                            .setMessage("O que deseja fazer com a mensagem?")
                            .setCancelable(true).
                            setPositiveButton("Mover para recebidas", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    ZLServices.getInstance().changeMessageStatus(m, ZLMessageType.ZLMessageTypeRead, true, MessagesActivity.this, new ZLServiceOperationCompleted<Boolean>() {

                                        @Override
                                        public void operationCompleted(ZLServiceResponse<Boolean> response) {
                                            if (response.errorMessage != null) {
                                                Toast.makeText(MessagesActivity.this, response.errorMessage, Toast.LENGTH_LONG).show();
                                            } else {
                                                archived.remove(pos);
                                                m.status = ZLMessageType.ZLMessageTypeRead;
                                                received.add(m);
                                                adapterArquivadas.notifyDataSetChanged();
                                            }
                                        }
                                    });
                                    dialog.cancel();
                                }
                            }).setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                        }
                    })
                            .create()
                            .show();
                } else if (m.status == ZLMessageType.ZLMessageTypeRead) {
                    // archivar
                    // marcar como não lida
                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(MessagesActivity.this);
                    // set title
                    alertDialogBuilder
                            .setTitle("Zolkin")
                            .setMessage("O que deseja fazer com a mensagem?")
                            .setCancelable(true).
                            setPositiveButton("Arquivar", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    ZLServices.getInstance().changeMessageStatus(m, ZLMessageType.ZLMessageTypeArchived, true, MessagesActivity.this, new ZLServiceOperationCompleted<Boolean>() {

                                        @Override
                                        public void operationCompleted(ZLServiceResponse<Boolean> response) {
                                            if (response.errorMessage != null) {
                                                Toast.makeText(MessagesActivity.this, response.errorMessage, Toast.LENGTH_LONG).show();
                                            } else {
                                                received.remove(pos);
                                                m.status = ZLMessageType.ZLMessageTypeArchived;
                                                archived.add(m);
                                                adapterRecebidas.notifyDataSetChanged();
                                            }
                                        }
                                    });
                                    dialog.cancel();
                                }
                            })
                            .setNeutralButton("Marcar como não lida", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    ZLServices.getInstance().changeMessageStatus(m, ZLMessageType.ZLMessageTypeUnread, true, MessagesActivity.this, new ZLServiceOperationCompleted<Boolean>() {

                                        @Override
                                        public void operationCompleted(ZLServiceResponse<Boolean> response) {
                                            if (response.errorMessage != null) {
                                                Toast.makeText(MessagesActivity.this, response.errorMessage, Toast.LENGTH_LONG).show();
                                            } else {
                                                m.status = ZLMessageType.ZLMessageTypeUnread;
                                                adapterRecebidas.notifyDataSetChanged();
                                            }
                                        }
                                    });
                                    dialog.cancel();
                                }
                            })
                            .setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.cancel();
                                }
                            })
                            .create()
                            .show();
                }

                return true;
            }
        });
    }

    // configure button visuals
    // change adapter
    void showReceived() {
        tvBtRecebidas.setBackgroundResource(R.drawable.segmented_left_selected);
        tvBtArquivadas.setBackgroundResource(R.drawable.segmented_right_unselected);
        tvBtRecebidas.setTextColor(this.getResources().getColor(R.color.zl_green));
        tvBtArquivadas.setTextColor(this.getResources().getColor(R.color.zl_purple));
        if (adapterRecebidas != null)
            lvMessages.setAdapter(adapterRecebidas);
        showingArchivedMessages = false;
    }

    // configure button visuals
    // change adapter
    void showArchivedMessages() {
        tvBtRecebidas.setBackgroundResource(R.drawable.segmented_left_unselected);
        tvBtArquivadas.setBackgroundResource(R.drawable.segmented_right_selected);
        tvBtRecebidas.setTextColor(this.getResources().getColor(R.color.zl_purple));
        tvBtArquivadas.setTextColor(this.getResources().getColor(R.color.zl_green));
        if (adapterArquivadas != null)
            lvMessages.setAdapter(adapterArquivadas);
        showingArchivedMessages = true;
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (adapterArquivadas != null)
            adapterArquivadas.notifyDataSetChanged();

        if (adapterRecebidas != null)
            adapterRecebidas.notifyDataSetChanged();

        ZLServices.getInstance().getMessages(true, this, new ZLServiceOperationCompleted<List<ZLMessage>>() {

            @Override
            public void operationCompleted(ZLServiceResponse<List<ZLMessage>> response) {
                if (response.errorMessage != null) {
                    Toast.makeText(MessagesActivity.this, response.errorMessage, Toast.LENGTH_LONG).show();
                } else {
                    // split into two lists
                    received = new ArrayList<ZLMessage>();
                    archived = new ArrayList<ZLMessage>();

                    for (int i = 0; i < response.serviceResponse.size(); i++) {
                        ZLMessage m = response.serviceResponse.get(i);
                        if (m.status == ZLMessageType.ZLMessageTypeArchived)
                            archived.add(m);
                        else
                            received.add(m);
                    }

                    adapterRecebidas = new MessagesAdapter(MessagesActivity.this, received);
                    adapterArquivadas = new MessagesAdapter(MessagesActivity.this, archived);

                    if (showingArchivedMessages)
                        lvMessages.setAdapter(adapterArquivadas);
                    else
                        lvMessages.setAdapter(adapterRecebidas);

                    //check for pending action
                    if (pendingAction != null) {

                        //process pending action
                        switch (pendingAction.actionType) {
                            case ZLPendingActionTypeSurvey:
                                for (int i = 0; i < response.serviceResponse.size(); i++) {
                                    ZLMessage m = response.serviceResponse.get(i);
                                    if (m.idPesquisa != null) {
                                        if (m.idPesquisa.equals(pendingAction.param)) {
                                            if (m.answered) {
                                                pendingAction = null;
                                                Toast.makeText(MessagesActivity.this, "Você já respondeu esta pesquisa.", Toast.LENGTH_LONG).show();
                                                return;
                                            }
                                            SurveyActivity.message = m;
                                            pendingAction = null;
                                            startActivity(new Intent(MessagesActivity.this, SurveyActivity.class));
                                            break;
                                        }
                                    }
                                }
                                break;
                            case ZLPendingActionTypeMessage:
                                for (int i = 0; i < response.serviceResponse.size(); i++) {
                                    ZLMessage m = response.serviceResponse.get(i);

                                    if (m.messageID.equals(pendingAction.param)) {
                                        MessageActivity.message = m;
                                        pendingAction = null;
                                        startActivity(new Intent(MessagesActivity.this, MessageActivity.class));
                                        break;
                                    }
                                }
                                break;
                            default:
                                break;
                        }
                    }
                }
            }
        });
    }
}
