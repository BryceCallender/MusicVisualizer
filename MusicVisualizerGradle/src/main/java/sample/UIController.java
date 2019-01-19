package sample;

import Music.MusicController;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;

import java.io.*;
import java.net.URL;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;


public class UIController implements Initializable
{
    @FXML
    private Label nowPlayingLabel;
    @FXML
    private ChoiceBox<String> songChoices;
    @FXML
    private Slider heightSlider;
    @FXML
    private Button playBackButton;
    @FXML
    private Button songPlayButton;
    @FXML
    private Button shuffleButton;
    @FXML
    private ColorPicker rectangleColorPicker;
    @FXML
    private CheckBox rainbowCheckBox;
    @FXML
    private Slider volumeSlider;
    @FXML
    private ProgressBar songProgressBar;

    //Lists for the players,their respective controllers, and link in jar file or in ide to find
    private ArrayList<MediaPlayer> players;
    private ArrayList<MusicController> musicControllers;
    private ArrayList<String> urls;

    //Tracks the songs at their index, volume, or path to find if they select one song at a time
    private int songIndex = 0;
    private double volume = 100;
    private String pathToFind = "";

    private boolean isJar = false;

    @Override
    public void initialize(URL location, ResourceBundle resources)
    {
        nowPlayingLabel.setStyle("-fx-background-color: black;");
        nowPlayingLabel.setText("");

        players = new ArrayList<>();
        musicControllers = new ArrayList<>();
        urls = new ArrayList<>();

        //Says when a slider value has been changed
        heightSlider.valueProperty().addListener((observable, oldValue, newValue) ->
        {
            heightSlider.setValue(newValue.doubleValue());
        });

        volumeSlider.setValue(100);

        //Says when a slider value has been changed
        volumeSlider.valueProperty().addListener((observable, oldValue, newValue) ->
        {
            for (MediaPlayer player: players)
            {
                double volume = newValue.doubleValue()/100.0;
                player.setVolume(volume);
                this.volume = volume;
            }
        });

        final String path = "Music/";
        final File jarFile = new File(getClass().getProtectionDomain().getCodeSource().getLocation().getPath());

        //If we are dealing with a jar file
        if(jarFile.isFile())
        {
            // Run with JAR file
            final JarFile jar;
            try
            {
                jar = new JarFile(jarFile);
                isJar = true;
                final Enumeration<JarEntry> entries = jar.entries(); //gives ALL entries in jar
                while (entries.hasMoreElements())
                {
                    final String name = entries.nextElement().getName();
                    if(name.contains(path) && name.contains(".mp3"))
                    {
                        //Add the song name
                        songChoices.getItems().add(name.substring(name.indexOf('/') + 1));
                        //Add the path since only this works for java jar files it seems to need the jar: that it
                        //gets when assigned into the jar entry so i cash it for later use for the song player
                        urls.add((Main.class.getResource("/Music/" + name.substring(name.indexOf('/') + 1)).toString()));
                    }

                }
                if(urls.size() > 0)
                {
                    pathToFind = urls.get(0).substring(0,urls.get(0).lastIndexOf('/') + 1);
                }

                jar.close();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
        //Executing in an IDE
        else
        {
            File directory = new File("src/main/resources/Music");
            File[] files = directory.listFiles();

            if(files == null)
            {
                System.out.println("Uh oh something isn't going right here");
            }
            else
            {
                for (File file:files)
                {
                    if(!file.getName().startsWith(".") && file.getName().contains(".mp3"))
                    {
                        songChoices.getItems().add(file.getName());
                    }
                }
            }
        }
    }

    public String getSongName()
    {
        return songChoices.getValue();
    }

    public void onSkip()
    {
        if(players.size() > 0)
        {
            players.get(0).stop();
            musicControllers.get(0).pause();

            players.remove(0);
            musicControllers.remove(0);

            //Keep index at 0 for the lowest
            if(songIndex > 0)
            {
                songIndex--;
            }

            changePlayButtonText();
            //If we still have a song left in the list
            if(players.size() == 0)
            {
                nowPlayingLabel.setText("");
            }
            else
            {
                displaySongName();
                setSongProgressBar(0.0);
                getMusicController().play();
                applyVolume();
            }
        }
    }

    public void onPlayBack()
    {
        if(players.size() > 0)
        {
            MediaPlayer.Status status = players.get(0).getStatus();
            applyVolume();
            if(status == MediaPlayer.Status.PLAYING)
            {
                players.get(0).pause();
                playBackButton.setText("Resume");
            }
            if(status == MediaPlayer.Status.PAUSED)
            {
                players.get(0).play();
                playBackButton.setText("Pause");

            }
        }
    }

    public double sliderValue()
    {
        return heightSlider.getValue();
    }

    private ArrayList<MediaPlayer> getPlayers()
    {
        return players;
    }

    public ArrayList<MusicController> getMusicControllers()
    {
        return musicControllers;
    }

    public ArrayList<String> getUrls()
    {
        return urls;
    }

    public void playButtonPressed()
    {
        if(songChoices.getValue() == null)
        {
            return;
        }

        Media media;
        if(isJar)
        {
            media = new Media(urls.get(getSongIndex(getSongName())));
        }
        else
        {
            media = new Media(new File(Main.class.getResource("/Music/" + getSongName()).getFile()).toURI().toString());
        }

        getPlayerReady(media);

        if(getCurrentMediaPlayer().getStatus() != MediaPlayer.Status.PLAYING)
        {
            playNextSong();
        }
    }

    private void playNextSong()
    {
        if(players.size() == 0)
        {
            nowPlayingLabel.setText("");
        }
        else if(getCurrentMediaPlayer().getStatus() != MediaPlayer.Status.PLAYING && players.size() > 0)
        {
            setSongProgressBar(0.0);
            getMusicController().play();
            displaySongName();
        }
    }

    private void setUpMusicController(MediaPlayer player)
    {
        MusicController musicController = new MusicController(player);
        musicControllers.add(musicController);
    }

    public MediaPlayer getCurrentMediaPlayer()
    {
        if(players.size() > 0)
        {
            return players.get(0);
        }
        else
        {
            return null;
        }
    }

    public boolean rainbowChecked()
    {
        return rainbowCheckBox.isSelected();
    }

    public Color getColor()
    {
        rainbowCheckBox.setSelected(false);
        return rectangleColorPicker.getValue();
    }

    public MusicController getMusicController()
    {
        if(musicControllers.size() > 0)
        {
            return musicControllers.get(0);
        }
        else
        {
            return null;
        }
    }

    public void changePlayButtonText()
    {
        if(getCurrentMediaPlayer() == null)
        {
            songPlayButton.setText("Play Song!");
            return;
        }
        //If nothing is in the player queue and the player isn't currently playing say add song
        if(players.size() == 0 && getCurrentMediaPlayer().getStatus() != MediaPlayer.Status.PLAYING)
        {
            nowPlayingLabel.setText("");
            songPlayButton.setText("Play Song!");
        }
        else
        {
            songPlayButton.setText("Queue Song!");
        }
    }

    private void displaySongName()
    {
        //Gets rid of the _ from the song names since spaces caused errors
        String songNameLabelInfo = getCurrentMediaPlayer().getMedia().getSource().replace("_", " ");
        songNameLabelInfo = songNameLabelInfo.substring(songNameLabelInfo.lastIndexOf("/") + 1);
        //Gets rid of the .mp3 attached to the end because they dont need to see that just the name of the song
        songNameLabelInfo = songNameLabelInfo.substring(0,songNameLabelInfo.lastIndexOf("."));
        nowPlayingLabel.setText("Now Playing: " + songNameLabelInfo);
    }

    private void applyVolume()
    {
        for (MediaPlayer player: players)
        {
            player.setVolume(volume);
        }
    }

    public void setSongProgressBar(double value)
    {
        if(value == 0)
        {
            songProgressBar.setProgress(0);
        }
        else
        {
            double modifiedValue = value / getCurrentMediaPlayer().getTotalDuration().toSeconds();
            songProgressBar.setProgress(modifiedValue);
        }

    }

    private void getPlayerReady(Media media)
    {
        players.add(new MediaPlayer(media));
        setUpMusicController(players.get(songIndex));
        changePlayButtonText();
        applyVolume();
        songIndex++;
        for (MediaPlayer player: players)
        {
            player.setOnEndOfMedia(new Runnable()
            {
                @Override public void run()
                {
                    if(players.size() > 0)
                    {
                        players.remove(0);
                        musicControllers.remove(0);
                    }
                    playNextSong();

                    if(songIndex > 0)
                    {
                        songIndex--;
                    }
                }
            });
        }
    }

    public void shuffleSongs()
    {
        //number of songs in the player already that the user decided to put in so we must take in account they
        //want to hear these and not let them be skipped or shuffled somewhere else
        int numberInQueue = players.size();
        for (String songName: songChoices.getItems())
        {
            Media media;
            if(isJar)
            {
                media = new Media(urls.get(songIndex));
            }
            else
            {
                media = new Media(new File(Main.class.getResource("/Music/" + songName).getFile()).toURI().toString());
            }

            getPlayerReady(media);
        }

        //Works since its the same random seed so it'll generate the same random sequence
        //random numbers are pseduorandom, not random in nature.
        long seed = System.nanoTime();
        //If music is playing just shuffle the songs onto the end of the list
        if(getCurrentMediaPlayer().getStatus() == MediaPlayer.Status.PLAYING)
        {
            Collections.shuffle(players.subList(numberInQueue,players.size()), new Random(seed));
            Collections.shuffle(musicControllers.subList(numberInQueue,musicControllers.size()), new Random(seed));
            Collections.shuffle(urls.subList(numberInQueue,urls.size()), new Random(seed));
        }
        else
        {
            //Shuffle everything and play
            Collections.shuffle(players, new Random(seed));
            Collections.shuffle(musicControllers, new Random(seed));
            Collections.shuffle(urls, new Random(seed));
            playNextSong();
        }
    }

    private int getSongIndex(String songName)
    {
        for (int i = 0; i < urls.size(); i++)
        {
            if(urls.get(i).equals(pathToFind + songName))
            {
                return i;
            }
        }
        return -1;
    }

    public void clearSongs()
    {
        songIndex = 0;
        getCurrentMediaPlayer().stop();
        getMusicController().pause();
        players.clear();
        musicControllers.clear();
        nowPlayingLabel.setText("");
    }
}
