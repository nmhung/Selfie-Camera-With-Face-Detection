# Selfie Camera with Face Detection

This library allow you to use [Firebase Machine Learning - Face
Detection](https://firebase.google.com/docs/ml-kit/detect-faces) in your
camera to taking selfie picture.

With the face detection benefit, you can easily implement the  
`force user to take a selfie picture with their face` function.

## Video Walkthrough

Here's a walkthrough:

<img src="/art/mlcamera demo.gif?raw=true" width="320px">


## Usage
Step 1: Start the Selfie Activity in your app:

```
 btn_open_camera.setOnClickListener {
        startActivityForResult(Intent(this, SelfieActivity::class.java), SELFIE_REQUEST_CODE)
 }
```

Step 2: Receive the imagePath in `onActivityResult` function.

```
override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == SELFIE_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            val imagePath = data?.getStringExtra(SelfieActivity.KEY_IMAGE_PATH)
            Toast.makeText(
                this,
                imagePath,
                Toast.LENGTH_SHORT
            ).show()
            val bmImg = BitmapFactory.decodeFile(imagePath)
            profile_image.setImageBitmap(bmImg)
        }
    }
```

## Download
             implementation 'net.fitken:mlselfiecamera:0.1.2'




