package sample;

import javafx.application.Application;

import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.scene.shape.*;

import java.util.Timer;
import java.util.TimerTask;

public class Main extends Application
{
    @Override
    public void start(Stage primaryStage) throws Exception
    {
        final int MAX_RECTS = 128; //Based on number of bands the audio listener has implemented
        final double MAX_RGB_VALUE = 330.0;

        FXMLLoader loader = new FXMLLoader(Main.class.getResource("/Visualizer.fxml"));
        BorderPane root = loader.load();

        UIController uiController = loader.getController();

        Scene scene = new Scene(root, 1000, 700);

        Pane pane = (Pane)root.getCenter();
        pane.setStyle("-fx-background-color: black;");


        double offset = scene.getWidth()/MAX_RECTS;
        double step = MAX_RGB_VALUE/MAX_RECTS;

        /*******************************************
                SETS UP INITIAL RECTANGLES
         *******************************************/
        for(int i = 0; i < MAX_RECTS; i++)
        {
            Rectangle r = new Rectangle(offset * i ,scene.getHeight()-100,offset,0);
            r.setStroke(Color.BLACK);
            if(uiController.rainbowChecked())
            {
                r.setFill(Color.hsb(i*step,1.0,1.0));
            }

            pane.getChildren().add(r);
        }

        /*******************************************
                 GUI UPDATING FUNCTION
         *******************************************/
        Timer timer = new Timer();
        TimerTask task = new TimerTask()
        {
            @Override
            public void run()
            {
                Platform.runLater(() ->
                {
                    //Make sure since it'll run regardless to check if we even have a media player playing right now
                    if(uiController.getCurrentMediaPlayer() != null)
                    {
                        //Go to the end since the very right is the higher frequencies of the song [0,22050] Hz
                        int counter = pane.getChildren().size() - 1;
                        int index = 0;

                        uiController.setSongProgressBar(uiController.getCurrentMediaPlayer().getCurrentTime().toSeconds());

                        //System.out.println(uiController.getCurrentMediaPlayer().getCurrentTime().toSeconds());
                        //Loop through all the items in the window
                        for (Node node : pane.getChildren())
                        {
                            if (node instanceof Rectangle)
                            {
                                //Use rainbow color based on if they want it or not!
                                if (uiController.rainbowChecked())
                                {
                                    ((Rectangle) node).setFill(Color.hsb(index * step, 1.0, 1.0));
                                }
                                else
                                {
                                    ((Rectangle) node).setFill(uiController.getColor());
                                }

                                //Calculate new offset since window might expand might be a bit calculation heavy
                                //however this works fine
                                double newOffset = pane.getWidth() / MAX_RECTS;
                                //Get the value at the respective location
                                double currentMusicValue = uiController.getMusicController().getValues()[counter] * uiController.sliderValue();

                                /*
                                The x and y coordinates should change as the window is expanding, change the
                                setResize to true and this works for resizing. It is here to be expanded upon if
                                anyone wants to change this program to their needs or update it :)
                                 */

                                //Set the Y coordinate of the rectangle so it should be located at where the peak of it is
                                //since the rectangle is located based on the top left of the rectangle
                                ((Rectangle) node).setY(pane.getHeight() - currentMusicValue);
                                //Set the X coordinate of the rectangle
                                ((Rectangle) node).setX(pane.getWidth() - counter * newOffset);
                                //Set the height based on the value that is being output
                                ((Rectangle) node).setHeight(currentMusicValue);
                            }
                            //Decrement the counter which is the position of the values in the respective array
                            counter--;
                            index++;
                        }
                    }
                    else
                    {
                        uiController.changePlayButtonText();
                        uiController.setSongProgressBar(0.0);
                        //Clears screen
                        for (Node node : pane.getChildren())
                        {
                            if (node instanceof Rectangle)
                            {
                                ((Rectangle) node).setHeight(0);
                            }
                        }
                    }
                });
            }
        };

        //This deals with allowing the task to be done at 60 FPS, well theoretically since every 17ms
        //it schedules a GUI update
        try
        {
            timer.schedule(task,0,17);
        }
        catch(NullPointerException e)
        {
            e.printStackTrace();
        }

        //Stage setting up
        primaryStage.setTitle("Music Visualizer");
        primaryStage.setScene(scene);
        primaryStage.show();
        primaryStage.setResizable(false);

        //Define how the window will close
        primaryStage.setOnCloseRequest(t ->
        {
            Platform.exit();
            System.exit(0);
        });
    }
    
    public static void main(String[] args)
    {
        launch(args);
    }
}
