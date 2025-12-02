//package com.fictivestudios.hush.utils
//
//import android.media.MediaCodec
//import android.media.MediaExtractor
//import android.media.MediaFormat
//import android.media.MediaMuxer
//import android.util.Log
//import java.io.File
//import java.io.IOException
//
//class VideoCompressor {
//
//    fun compressVideo(inputPath: String, outputPath: String): File {
//        try {
//            val mediaExtractor = MediaExtractor()
//            mediaExtractor.setDataSource(inputPath)
//
//            val mediaFormat = getVideoFormat(mediaExtractor)
//
//            val mediaCodec = MediaCodec.createEncoderByType(MediaFormat.MIMETYPE_VIDEO_AVC)
//            val bitrate = determineBitrate(mediaFormat)
//            mediaFormat.setInteger(MediaFormat.KEY_BIT_RATE, bitrate)
//            mediaCodec.configure(mediaFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
//            mediaCodec.start()
//
//            val mediaMuxer = MediaMuxer(outputPath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)
//
//            val trackIndex = mediaMuxer.addTrack(mediaFormat)
//            mediaMuxer.start()
//
//            val bufferInfo = MediaCodec.BufferInfo()
//            val inputBufferIndex = mediaCodec.dequeueInputBuffer(-1)
//            if (inputBufferIndex >= 0) {
//                val inputBuffer = mediaCodec.getInputBuffer(inputBufferIndex)
//                val sampleSize = mediaExtractor.readSampleData(inputBuffer!!, 0)
//                if (sampleSize < 0) {
//                    mediaCodec.queueInputBuffer(
//                        inputBufferIndex,
//                        0,
//                        0,
//                        0,
//                        MediaCodec.BUFFER_FLAG_END_OF_STREAM
//                    )
//                } else {
//                    mediaCodec.queueInputBuffer(
//                        inputBufferIndex,
//                        0,
//                        sampleSize,
//                        mediaExtractor.sampleTime,
//                        0
//                    )
//                    mediaExtractor.advance()
//                }
//            }
//
//            while (true) {
//                val outputBufferIndex = mediaCodec.dequeueOutputBuffer(bufferInfo, -1)
//                if (outputBufferIndex >= 0) {
//                    val outputBuffer = mediaCodec.getOutputBuffer(outputBufferIndex)
//                    mediaMuxer.writeSampleData(trackIndex, outputBuffer!!, bufferInfo)
//                    mediaCodec.releaseOutputBuffer(outputBufferIndex, false)
//                    if (bufferInfo.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM != 0) break
//                } else if (outputBufferIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
//                    // Ignore
//                }
//            }
//
//            mediaExtractor.release()
//            mediaCodec.stop()
//            mediaCodec.release()
//            mediaMuxer.stop()
//            mediaMuxer.release()
//
//            Log.d(
//                "com.fictivestudios.hush.utils.VideoCompressor",
//                "Compression completed successfully"
//            )
//
//        } catch (e: IOException) {
//            Log.e("com.fictivestudios.hush.utils.VideoCompressor", "IOException: ${e.message}")
//        } catch (e: IllegalArgumentException) {
//            Log.e(
//                "com.fictivestudios.hush.utils.VideoCompressor",
//                "IllegalArgumentException: ${e.message}"
//            )
//        } catch (e: Exception) {
//            Log.e("com.fictivestudios.hush.utils.VideoCompressor", "Exception: ${e.message}")
//        }
//        return File(outputPath)
//    }
//
//    private fun getVideoFormat(mediaExtractor: MediaExtractor): MediaFormat {
//        for (i in 0 until mediaExtractor.trackCount) {
//            val format = mediaExtractor.getTrackFormat(i)
//            val mime = format.getString(MediaFormat.KEY_MIME)
//            if (mime?.startsWith("video/") == true) {
//                return format
//            }
//        }
//        throw IllegalArgumentException("No video track found")
//    }
//
//    private fun determineBitrate(mediaFormat: MediaFormat): Int {
//        // Determine bitrate based on video resolution, frame rate, and desired quality
//        // Adjust this based on your requirements
//        val width = mediaFormat.getInteger(MediaFormat.KEY_WIDTH)
//        val height = mediaFormat.getInteger(MediaFormat.KEY_HEIGHT)
//        val frameRate = mediaFormat.getInteger(MediaFormat.KEY_FRAME_RATE)
//        val bitrate = width * height * frameRate / 10 // Adjust this value as needed
//        return bitrate
//    }
//}
