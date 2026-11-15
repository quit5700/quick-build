package cn.quit5700.buildingwand.aux;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class AuxClickInputTest {
 @Test void shortPressSurvivesUntilTickAndIsConsumedOnce(){var input=new AuxClickInput();input.press(true,true,false);assertEquals(AuxClickInput.Click.LEFT,input.poll());assertNull(input.poll());}
 @Test void menuClickCannotAttachOrConfirmAfterClosingScreen(){var input=new AuxClickInput();input.press(false,true,false);assertNull(input.poll());input.press(true,true,false);assertEquals(AuxClickInput.Click.LEFT,input.poll());assertNull(input.poll());}
 @Test void orderedModeSwitchAndConfirmationRemainSeparate(){var input=new AuxClickInput();input.press(true,false,true);input.press(true,true,false);assertEquals(AuxClickInput.Click.RIGHT,input.poll());assertEquals(AuxClickInput.Click.LEFT,input.poll());assertNull(input.poll());}
 @Test void cancellationDropsPendingActions(){var input=new AuxClickInput();input.press(true,true,false);input.press(true,false,true);input.clear();assertNull(input.poll());}
 @Test void unrelatedMouseButtonsAreIgnored(){var input=new AuxClickInput();input.press(true,false,false);assertNull(input.poll());}
}
