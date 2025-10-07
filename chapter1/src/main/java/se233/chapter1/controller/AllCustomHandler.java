package se233.chapter1.controller;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.*;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import se233.chapter1.Launcher;
import se233.chapter1.model.character.BattlemageCharacter;
import se233.chapter1.model.item.BasedEquipment;
import se233.chapter1.model.character.BasedCharacter;
import se233.chapter1.model.item.Weapon;
import se233.chapter1.model.item.Armor;
import java.util.ArrayList;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger; //exercise6.1

public class AllCustomHandler {
    private static final Logger logger = LogManager.getLogger(AllCustomHandler.class); //exercise6.1
    public static class GenCharacterHandler implements EventHandler<ActionEvent> {
        @Override
        public void handle(ActionEvent event) {
            //keep the original weapon&armor before clear
            Weapon currentWeapon = Launcher.getEquippedWeapon();
            Armor currentArmor = Launcher.getEquippedArmor();

            //add back to inventory
            ArrayList<BasedEquipment> allEquipments = Launcher.getAllEquipments();
            if (currentWeapon != null) {
                allEquipments.add(currentWeapon);
            }
            if (currentArmor != null) {
                allEquipments.add(currentArmor);
            }
            Launcher.setAllEquipments(allEquipments);

            Launcher.setMainCharacter(GenCharacter.setUpCharacter());
            Launcher.setEquippedWeapon(null);
            Launcher.setEquippedArmor(null);
            Launcher.refreshPane();
        }
    }

    public static void onDragDetected(MouseEvent event, BasedEquipment equipment, ImageView imgView) {
        Dragboard db = imgView.startDragAndDrop(TransferMode.ANY);
        db.setDragView(imgView.getImage());
        ClipboardContent content = new ClipboardContent();
        content.put(BasedEquipment.DATA_FORMAT, equipment);
        db.setContent(content);
        event.consume();
    }

    public static void onDragOver(DragEvent event, String type) {
        Dragboard dragboard = event.getDragboard();
        BasedEquipment retrieveEquipment = (BasedEquipment)dragboard.getContent(BasedEquipment.DATA_FORMAT);
        if(dragboard.hasContent(BasedEquipment.DATA_FORMAT) && retrieveEquipment.getClass().getSimpleName().equals(type)) {
            event.acceptTransferModes(TransferMode.MOVE);
        }
    }

    public static void onDragDropped(DragEvent event, Label lbl, StackPane imgGroup) {
        boolean dragCompleted = false;
        Dragboard dragboard = event.getDragboard();
        ArrayList<BasedEquipment> allEquipments = Launcher.getAllEquipments();

        if (dragboard.hasContent(BasedEquipment.DATA_FORMAT)) {
            BasedEquipment retrieveEquipment = (BasedEquipment) dragboard.getContent(BasedEquipment.DATA_FORMAT);
            BasedCharacter character = Launcher.getMainCharacter();

            if (retrieveEquipment instanceof Weapon) {
                Weapon weapon = (Weapon) retrieveEquipment;

                if (character instanceof BattlemageCharacter || character.getType() == weapon.getDamageType()) {
                    if (Launcher.getEquippedWeapon() != null) {
                        allEquipments.add(Launcher.getEquippedWeapon());
                    }
                    Launcher.setEquippedWeapon(weapon);
                    character.equipWeapon(weapon);
                } else {
                    System.out.println("Weapon type does not match character type!");
                    allEquipments.add(retrieveEquipment);
                    Launcher.setAllEquipments(allEquipments);
                    event.setDropCompleted(false);
                    Launcher.refreshPane();
                    return;
                }

            } else if (retrieveEquipment instanceof Armor) {
                if (character instanceof BattlemageCharacter) {
                    System.out.println("Battlemage cannot equip armor!");
                    allEquipments.add(retrieveEquipment);
                    Launcher.setAllEquipments(allEquipments);
                    event.setDropCompleted(false);
                    Launcher.refreshPane();
                    return;
                } else {
                    Armor armor = (Armor) retrieveEquipment;
                    if (Launcher.getEquippedArmor() != null) {
                        allEquipments.add(Launcher.getEquippedArmor());
                    }
                    Launcher.setEquippedArmor(armor);
                    character.equipArmor(armor);
                    logger.info("Equipped Armor: " + armor.getName()); //exercise6.1
                }
            }

            Launcher.setMainCharacter(character);
            Launcher.setAllEquipments(allEquipments);
            Launcher.refreshPane();

            ImageView imgView = new ImageView();
            if (imgGroup.getChildren().size() != 1) {
                imgGroup.getChildren().remove(1);
            }

            lbl.setText(retrieveEquipment.getClass().getSimpleName() + ":\n" + retrieveEquipment.getName());
            imgView.setImage(new Image(Launcher.class.getResource(retrieveEquipment.getImagepath()).toString()));
            imgGroup.getChildren().add(imgView);
            dragCompleted = true;
        }
        event.setDropCompleted(dragCompleted);
    }


    public static void onEquipDone(DragEvent event) {
        Dragboard dragboard = event.getDragboard();
        ArrayList<BasedEquipment> allEquipments = Launcher.getAllEquipments();
        BasedEquipment retrieveEquipment = (BasedEquipment)dragboard.getContent(BasedEquipment.DATA_FORMAT);

        int pos = -1;
        for(int i=0; i<allEquipments.size(); i++) {
            if (allEquipments.get(i).getName().equals(retrieveEquipment.getName())) {
                pos = i;
            }
        }

        if (pos != -1) {
            allEquipments.removeIf(allEquipment -> event.isAccepted() && allEquipment.getName().equals(retrieveEquipment.getName()));
        }

        Launcher.setAllEquipments(allEquipments);
        Launcher.refreshPane();
    }
}