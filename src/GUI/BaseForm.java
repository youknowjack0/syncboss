package GUI;

import Shared.StateManager;
import Shared.ListObject;

import javax.swing.*;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;
import javax.sound.sampled.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.util.Vector;
import java.util.prefs.Preferences;
import java.net.URL;
import java.io.IOException;

import Server.*;
import MediaPlayer.tester;
import MediaPlayer.OffsetObject;
import MediaPlayer.MediaTransmitter;
import MediaPlayer.SimpleMediaPlayer;

/**
 * Created by IntelliJ IDEA.
 * User: Jack
 * Date: 28/06/2009
 * Time: 10:00:02 AM
 */
public class BaseForm implements VolumeRegistry, OffsetRegistry {
    private JButton startBitchModeButton;
    private JButton startRequestDJModeButton;
    private JTextField djAddressField;


    public JButton getStartBitchModeButton() {
        return startBitchModeButton;
    }

    public JButton getStartRequestDJModeButton() {
        return startRequestDJModeButton;
    }

    public JTextField getDjAddressField() {
        return djAddressField;
    }

    public JButton getSelectInputSourceButton() {
        return selectInputSourceButton;
    }

    public JButton getForceReSyncAllButton() {
        return forceReSyncAllButton;
    }

    public JLabel getCurrentModeLabel() {
        return currentModeLabel;
    }

    public JPanel getBasePanel() {
        return basePanel;
    }

    public JSlider getPlaybackOffsetSlider() {
        return playbackOffsetSlider;
    }

    public JSlider getVolumeControl() {
        return volumeControl;
    }

    public JComboBox getOutputDeviceSelect() {
        return outputDeviceSelect;
    }

    public Vector<Control> getVolumeRegistry() {
        return volumeRegistry;
    }

    public Vector<OffsetObject> getOffsetRegistry() {
        return offsetRegistry;
    }

    public VolumeRegistry getSelfVolumeRegistry() {
        return selfVolumeRegistry;
    }

    public OffsetRegistry getSelfOffsetRegistry() {
        return selfOffsetRegistry;
    }

    private JButton selectInputSourceButton;
    private JButton forceReSyncAllButton;
    private JLabel currentModeLabel;
    private JPanel basePanel;
    private JSlider playbackOffsetSlider;
    private JSlider volumeControl;
    private JComboBox outputDeviceSelect;

    public JSlider getSyncMonitor() {
        return syncMonitor;
    }

    private JSlider syncMonitor;
    private JButton forceReSyncButton;
    private Vector<Control> volumeRegistry = new Vector<Control>();
    private Vector<OffsetObject> offsetRegistry = new Vector<OffsetObject>();
    private VolumeRegistry selfVolumeRegistry = this;
    private OffsetRegistry selfOffsetRegistry = this;

    public void registerControl(Control ctrl) {
        volumeRegistry.add(ctrl);
    }

    public void unregisterControl(Control ctrl) {
        volumeRegistry.remove(ctrl);
    }

    public void updateVolumeControls() {
        for (Control ctrl : volumeRegistry) {
            FloatControl fctrl = (FloatControl) ctrl;
            float vol = ((float) volumeControl.getValue()) / 100; //0.0 .. 1.0
            fctrl.setValue(fctrl.getMinimum() + vol * (fctrl.getMaximum() - fctrl.getMinimum())); //translate to native line control range
        }
    }

    public BaseForm() {

        startRequestDJModeButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (StateManager.setServerMode() != null) {
                    currentModeLabel.setText("DJ Mode.");
                    startRequestDJModeButton.setEnabled(false);
                    startBitchModeButton.setEnabled(false);
                    djAddressField.setEnabled(false);
                    selectInputSourceButton.setEnabled(true);
                    outputDeviceSelect.setEnabled(false);
                }                
            }
        });
        startBitchModeButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (djAddressField.getText() == null || djAddressField.getText().equals("")) {
                    JOptionPane.showMessageDialog(null, "Please enter DJ computer name/IP address.");
                } else {
                    ListObject sl = (ListObject)outputDeviceSelect.getSelectedItem();
                    SimpleMediaPlayer lp = new SimpleMediaPlayer((Mixer.Info)sl.getValue(),selfVolumeRegistry,selfOffsetRegistry);
                    if (StateManager.setClientMode(djAddressField.getText(), lp) != null) {
                        currentModeLabel.setText("Bitch Mode.");
                        System.out.printf("Saving dj_address: %s", djAddressField.getText());
                        StateManager.prefs.put("dj_address", djAddressField.getText());
                        startRequestDJModeButton.setEnabled(false);
                        startBitchModeButton.setEnabled(false);
                        djAddressField.setEnabled(false);
                        outputDeviceSelect.setEnabled(false);
                    }
                }
            }
        });
        volumeControl.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                // for each registered line control, update volume
                updateVolumeControls();
                // save to registry
                System.out.printf("Saving volume: %d\n", volumeControl.getValue());
                StateManager.prefs.putInt("volume", volumeControl.getValue());
            }
        });
        playbackOffsetSlider.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                // update registered offset control
                updateOffsets();
                // save to registry
                System.out.printf("Saving manual_offset: %d\n", playbackOffsetSlider.getValue());
                StateManager.prefs.putInt("manual_offset", playbackOffsetSlider.getValue());                
            }
        });
        selectInputSourceButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if(StateManager.isServer()) {
                    //todo: remove tester code:
                    MediaTransmitter mt = null;
                    //try {
                        //ListObject sl = (ListObject)outputDeviceSelect.getSelectedItem();
                        //SimpleMediaPlayer lp = new SimpleMediaPlayer((Mixer.Info)sl.getValue(),selfVolumeRegistry,selfOffsetRegistry);
                        //listener for winamp or other plugin
                        //AudioInputStream pluginInput = new AudioInputStream(StateManager.pluginSocket.getInputStream(),AudioSystem.getAudioInputStream(new URL("file:///C:/one.wav")).getFormat(), Long.MAX_VALUE);

                        //mt = new MediaTransmitter(StateManager.getServer(),lp);
                        //mt.sendFormat();
                        //mt.play();
                    //} catch (UnsupportedAudioFileException e1) {
                    //    e1.printStackTrace();
                    //} catch (IOException e1) {
                    //    e1.printStackTrace();
                    //}
                }
            }
        });
        forceReSyncButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                 JOptionPane pane = new JOptionPane(
                    "Delete sync profile & re-train?.");
                Object[] options = new String[] { "Yes", "No" };
                pane.setOptions(options);
                JDialog dialog = pane.createDialog(new JFrame(), "Confirm Re-train");
                dialog.show();
                Object obj = pane.getValue();
                int result = -1;
                for (int k = 0; k < options.length; k++)
                  if (options[k].equals(obj))
                    result = k;
                if (result == 0) {
                    StateManager.player.forceResync();
                }

            }
        });
    }


    public static void main(String[] args) {
        try {
            // Set System L&F
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        }
        catch (UnsupportedLookAndFeelException e) {
            // handle exception
        }
        catch (ClassNotFoundException e) {
            // handle exception
        }
        catch (InstantiationException e) {
            // handle exception
        }
        catch (IllegalAccessException e) {
            // handle exception
        }
        JFrame frame = new JFrame("BaseForm");
        frame.setContentPane(new BaseForm().basePanel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);




        frame.setTitle("SyncBoss v0.3 ($Revision$)");
    }

    private void createUIComponents() {
        Vector<Object> list = new Vector<Object>();
        Line.Info showAll = new Line.Info(SourceDataLine.class);
        for (Mixer.Info m : AudioSystem.getMixerInfo()) {

            Mixer mixer = AudioSystem.getMixer(m);        
            for (Line.Info lineinfo : mixer.getSourceLineInfo(showAll)) { //just check if there's a SDL line, dont care what it is
                list.add(new ListObject(m.getName(), m));
                break;
            }


        }

        outputDeviceSelect = new JComboBox(list);
        outputDeviceSelect.setSelectedIndex(0);

        // dj addr pref
        String dj = StateManager.prefs.get("dj_address", "");
        djAddressField = new JTextField();
        djAddressField.setText(dj);
        System.out.printf("Loaded dj_address: %s\n", dj);

        // volume pref
        int vol = StateManager.prefs.getInt("volume", 90);
        volumeControl = new JSlider();
        volumeControl.setValue(vol);
        System.out.printf("Loaded volume: %d\n", vol);

        // offset pref
        int offset = StateManager.prefs.getInt("manual_offset", 50);
        playbackOffsetSlider = new JSlider();
        playbackOffsetSlider.setValue(offset);
        System.out.printf("Loaded manual_offset: %d\n", offset);


        StateManager.setForm(this);
    }

    public void registerOffsetObject(OffsetObject offs) {
        offsetRegistry.add(offs);
    }

    public void unregisterOffsetObject(OffsetObject offs) {
        offsetRegistry.remove(offs);
    }

    public void updateOffsets() {
        for (OffsetObject o : offsetRegistry) {
            o.setOffset(this.playbackOffsetSlider.getValue());
        }
    }
}