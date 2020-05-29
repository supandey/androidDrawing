package com.example.acrylicpaint;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.BlurMaskFilter;
import android.graphics.Color;
import android.graphics.EmbossMaskFilter;
import android.graphics.MaskFilter;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Shader;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;

public class MainActivity extends AppCompatActivity implements ColorPickerDialog.OnColorChangedListener {

    private MyView myView;
    private static final int CHOOSE_IMAGE = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        myView = (MyView)findViewById(R.id.myView1);
    }

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

        final Paint mPaint = myView.getmPaint();

        mPaint.setXfermode(null);
        mPaint.setAlpha(0xFF);

        switch (id) {
            case R.id.extract_color_menu: {
                Toast.makeText(getApplicationContext(),
                        R.string.tap_to_extract_color,
                        Toast.LENGTH_LONG).show();
                myView.setExtractingColor(true);
                return true;
            }
            case R.id.normal_brush_menu:
                mPaint.setShader( null );
                mPaint.setMaskFilter(null);
                return true;
            case R.id.color_menu:
                new ColorPickerDialog(this, this, mPaint.getColor()).show();
                return true;
            case R.id.emboss_menu:
                mPaint.setShader( null );

                // Where did these magic numbers come from? What do they mean? Can I change them? ~TheOpenSourceNinja
                // Absolutely random numbers in order to see the emboss. asd! ~Valerio
                MaskFilter mEmboss = new EmbossMaskFilter(new float[] { 1, 1, 1 }, 0.4f, 6, 3.5f);
                mPaint.setMaskFilter(mEmboss);
                return true;
            case R.id.smudge_menu: {
                /* I considered making this what happens when the blur_menu item is selected, but
                 * that could surprise users who are used to blur_menu's previous functionality, so
                 * I made this new smudge_menu item instead. I don't like calling it "Smudge" because
                 * this isn't exactly the same as what Photoshop and GIMP refer to as "Smudge", but I
                 * couldn't think of a better name that isn't "Blur".
                 * ~TheOpenSourceNinja
                 */
                if( Build.VERSION.SDK_INT >= 17 ) {
                    /* Basically what we're doing here is copying the entire foreground bitmap,
                     * blurring it, then telling mPaint to use that instead of a solid color.
                     */

                    RenderScript rs = RenderScript.create( getApplicationContext( ) );
                    ScriptIntrinsicBlur script;
                    try {
                        script = ScriptIntrinsicBlur.create(rs, Element.RGBA_8888(rs));
                    } catch (android.renderscript.RSIllegalArgumentException e) {
                        script = ScriptIntrinsicBlur.create(rs, Element.U8_4(rs));
                    }
                    script.setRadius( 20f ); //The radius must be between 0 and 25. Smaller radius means less blur. I just picked 20 randomly. ~TheOpenSourceNinja

                    //copy the foreground: (n API level 18+, this will be really fast because it uses a shared memory model, thus not really copying everything)
                    Allocation input = Allocation.createFromBitmap( rs, myView.getmBitmap() );
                    script.setInput( input );

                    //allocate memory for the output:
                    Allocation output = Allocation.createTyped( rs, input.getType( ) );

                    //Blur the image:
                    script.forEach( output );

                    //Store the blurred image as a Bitmap object:
                    Bitmap blurred = Bitmap.createBitmap( myView.getmBitmap() );
                    output.copyTo( blurred );

                    //Tell mPaint to use the blurred image:
                    Shader shader = new BitmapShader( blurred, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP );
                    mPaint.setShader( shader );
                    return true;
                } else {
                    Toast.makeText( this.getApplicationContext( ),
                            R.string.ability_disabled_need_newer_api_level,
                            Toast.LENGTH_LONG ).show( );
                    return true;
                }
            }
            case R.id.blur_menu:
                mPaint.setShader( null );

                MaskFilter mBlur = new BlurMaskFilter(5, BlurMaskFilter.Blur.NORMAL);
                mPaint.setMaskFilter(mBlur);
                return true;
            case R.id.size_menu: {
                LayoutInflater inflater = ( LayoutInflater ) getSystemService( Context.LAYOUT_INFLATER_SERVICE );
                View layout = inflater.inflate( R.layout.brush,
                        (ViewGroup) findViewById( R.id.root ) );
                AlertDialog.Builder builder = new AlertDialog.Builder( this )
                        .setView( layout );
                builder.setTitle( R.string.choose_width );
                final AlertDialog alertDialog = builder.create( );
                alertDialog.show( );
                SeekBar sb = ( SeekBar ) layout.findViewById( R.id.brushSizeSeekBar );
                sb.setProgress( myView.getStrokeSize( ) );
                final TextView txt = ( TextView ) layout
                        .findViewById( R.id.sizeValueTextView );
                txt.setText( String.format(
                        getResources( ).getString( R.string.your_selected_size_is ),
                        myView.getStrokeSize( ) + 1 ) );
                sb.setOnSeekBarChangeListener( new SeekBar.OnSeekBarChangeListener( ) {
                    public void onProgressChanged( SeekBar seekBar,
                                                   final int progress, boolean fromUser ) {
                        // Do something here with new value
                        mPaint.setStrokeWidth( progress );
                        txt.setText( String.format(
                                getResources( ).getString(
                                        R.string.your_selected_size_is ), progress + 1 ) );
                    }

                    @Override
                    public void onStartTrackingTouch( SeekBar seekBar ) {
                        // TODO Auto-generated method stub
                    }

                    @Override
                    public void onStopTrackingTouch( SeekBar seekBar ) {
                        // TODO Auto-generated method stub
                    }
                } );
                return true;
            }
            case R.id.erase_menu: {
                LayoutInflater inflater_e = ( LayoutInflater ) getSystemService( Context.LAYOUT_INFLATER_SERVICE );
                View layout_e = inflater_e.inflate( R.layout.brush,
                        ( ViewGroup ) findViewById( R.id.root ) );
                AlertDialog.Builder builder_e = new AlertDialog.Builder( this )
                        .setView( layout_e );
                builder_e.setTitle( R.string.choose_width );
                final AlertDialog alertDialog_e = builder_e.create( );
                alertDialog_e.show( );
                SeekBar sb_e = ( SeekBar ) layout_e.findViewById( R.id.brushSizeSeekBar );
                sb_e.setProgress( myView.getStrokeSize( ) );
                final TextView txt_e = ( TextView ) layout_e
                        .findViewById( R.id.sizeValueTextView );
                txt_e.setText( String.format(
                        getResources( ).getString( R.string.your_selected_size_is ),
                        myView.getStrokeSize( ) + 1 ) );
                sb_e.setOnSeekBarChangeListener( new SeekBar.OnSeekBarChangeListener( ) {
                    public void onProgressChanged( SeekBar seekBar,
                                                   final int progress, boolean fromUser ) {
                        // Do something here with new value
                        mPaint.setStrokeWidth( progress );
                        txt_e.setText( String.format(
                                getResources( ).getString(
                                        R.string.your_selected_size_is ), progress + 1 ) );
                    }

                    public void onStartTrackingTouch( SeekBar seekBar ) {
                        // TODO Auto-generated method stub
                    }

                    public void onStopTrackingTouch( SeekBar seekBar ) {
                        // TODO Auto-generated method stub
                    }
                } );
                mPaint.setShader( null );
                mPaint.setXfermode( new PorterDuffXfermode( PorterDuff.Mode.CLEAR ) );
                return true;
            }
            case R.id.clear_all_menu: {
                myView.getmBitmap().eraseColor( Color.TRANSPARENT );
                return true;
            }
            case R.id.save_menu:
                takeScreenshot(true);
                break;
            case R.id.share_menu: {
                File screenshotPath = takeScreenshot( false );
                Intent i = new Intent( );
                i.setAction( Intent.ACTION_SEND );
                i.setType( "image/png" );
                i.putExtra( Intent.EXTRA_SUBJECT,
                        getString( R.string.share_title_template ) );
                i.putExtra( Intent.EXTRA_TEXT,
                        getString( R.string.share_text_template ) );
                i.putExtra( Intent.EXTRA_STREAM, Uri.fromFile( screenshotPath ) );
                try {
                    startActivity( Intent.createChooser( i,
                            getString( R.string.toolbox_share_title ) ) );
                } catch( android.content.ActivityNotFoundException ex ) {
                    Toast.makeText( this.getApplicationContext( ),
                            R.string.no_way_to_share,
                            Toast.LENGTH_LONG ).show( );
                }
                break;
            }
            case R.id.open_image_menu: {
                Intent intent = new Intent( );
                intent.setType( "image/*" ); //The argument is an all-lower-case MIME type - in this case, any image format.
                intent.setAction( Intent.ACTION_GET_CONTENT );
                intent.putExtra( Intent.EXTRA_ALLOW_MULTIPLE, false ); //This is false by default, but I felt that for code clarity it was better to be explicit: we only want one image
                startActivityForResult( Intent.createChooser( intent, getResources().getString( R.string.select_image_to_open ) ), CHOOSE_IMAGE );
                break;
            }
            case R.id.fill_background_with_color: {
                myView.setWaitingForBackgroundColor(true);
                Bitmap mBitmapBackground = myView.getmBitmapBackground();
                new ColorPickerDialog( this, this, mBitmapBackground.getPixel( 0, 0 ) ).show();
                return true;
            }
            case R.id.about_menu:
                startActivity(new Intent(this, AboutActivity.class));
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * This takes the screenshot of the whole screen. Is this a good thing?
     */
    private File takeScreenshot(boolean showToast) {
        View v = findViewById(R.id.CanvasId);
        v.setDrawingCacheEnabled(true);
        Bitmap cachedBitmap = v.getDrawingCache();
        Bitmap copyBitmap = cachedBitmap.copy(Bitmap.Config.RGB_565, true);
        v.destroyDrawingCache();
        FileOutputStream output = null;
        File file = null;
        try {
            File path = Places.getScreenshotFolder();
            Calendar cal = Calendar.getInstance();

            file = new File(path,

                    cal.get(Calendar.YEAR) + "_" + (1 + cal.get(Calendar.MONTH)) + "_"
                            + cal.get(Calendar.DAY_OF_MONTH) + "_"
                            + cal.get(Calendar.HOUR_OF_DAY) + "_"
                            + cal.get(Calendar.MINUTE) + "_" + cal.get(Calendar.SECOND)
                            + ".png");
            output = new FileOutputStream(file);
            copyBitmap.compress(Bitmap.CompressFormat.PNG, 100, output);
        } catch (FileNotFoundException e) {
            file = null;
            e.printStackTrace();
        } finally {
            if (output != null) {
                try {
                    output.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }

        if (file != null) {
            if (showToast)
                Toast.makeText(
                        getApplicationContext(),
                        String.format(
                                getResources().getString(
                                        R.string.saved_your_location_to),
                                file.getAbsolutePath()), Toast.LENGTH_LONG)
                        .show();
            // sending a broadcast to the media scanner so it will scan the new
            // screenshot.
            Intent requestScan = new Intent(
                    Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
            requestScan.setData(Uri.fromFile(file));
            sendBroadcast(requestScan);

            return file;
        } else {
            return null;
        }
    }

    @Override
    public void colorChanged(int color) {
        if ( myView.getWaitingForBackgroundColor() ) {
            myView.setWaitingForBackgroundColor(false);
            myView.getmBitmapBackground().eraseColor( color );
            //int[] colors = new int[ 1 ];
            //colors[ 0 ] = color;
            //contentView.mBitmapBackground = Bitmap.createBitmap( colors, contentView.mBitmapBackground.getWidth(), contentView.mBitmapBackground.getHeight(), contentView.mBitmapBackground.getConfig() );
        } else {
            // Changes the color of the action bar when the pencil color is changed
            if(Build.VERSION.SDK_INT >= 11) {
                ActionBar actionBar = getActionBar();
                ColorDrawable colorDrawable = new ColorDrawable(color);
                actionBar.setBackgroundDrawable(colorDrawable);
            }
            myView.getmPaint().setColor( color );

        }
    }
}
