/*----------------------------------------------------------------------------------------------
 *
 * This file is ArcSoft's property. It contains ArcSoft's trade secret, proprietary and
 * confidential information.
 *
 * The information and code contained in this file is only for authorized ArcSoft employees
 * to design, create, modify, or review.
 *
 * DO NOT DISTRIBUTE, DO NOT DUPLICATE OR TRANSMIT IN ANY FORM WITHOUT PROPER AUTHORIZATION.
 *
 * If you are not an intended recipient of this file, you must not copy, distribute, modify,
 * or take any action in reliance on it.
 *
 * If you have received this file in error, please immediately notify ArcSoft and
 * permanently delete the original and any copy of any file and any printout thereof.
 *
 *-------------------------------------------------------------------------------------------------*/
/*
 * abstypes.h
 *
 *
 */

#ifndef _ABS_TYPES_H_
#define _ABS_TYPES_H_

#include "amcomdef.h"

#define ABS_MAX_FACE_NUM						10
#define ABS_MAX_IMAGE_FACE_NUM					10
#define ABS_MAX_VIDEO_FACE_NUM					4

/* Error Definitions */

// No Error.
#define ABS_OK									0
// Unknown error.
#define ABS_ERR_UNKNOWN							-1
// Invalid parameter, maybe caused by NULL pointer.
#define ABS_ERR_INVALID_INPUT					-2
// User abort.
#define ABS_ERR_USER_ABORT						-3
// Unsupported image format.
#define ABS_ERR_IMAGE_FORMAT					-101
// Image Size unmatched.
#define ABS_ERR_IMAGE_SIZE_UNMATCH				-102
// Out of memory.
#define ABS_ERR_ALLOC_MEM_FAIL					-201
// No face input while faces needed.
#define ABS_ERR_NOFACE_INPUT					-903
// Interface version error. Wrong head file for so.
#define ABS_ERR_INTERFACE_VERSION				-999

static const MInt32 ABS_INTERFACE_VERSION							= 26370805;

// Begin of feature key definitions

// video and image
// Feature key for intensity of Skin Brighten. Intensity from 0 to 100.
static const MInt32 ABS_KEY_BASIC_SKIN_BRIGHTEN						= 0x02010901;
// Feature key for intensities of Skin Soften. Intensity from 0 to 100.
static const MInt32 ABS_KEY_BASIC_SKIN_SOFTEN						= 0x02010b01;
// Feature key for intensities of Eye Enlargement. Intensity from -100 to 100.
static const MInt32 ABS_KEY_SHAPE_EYE_ENLARGEMENT					= 0x02040201;
// Feature key for intensities of Nose Highlight. Intensity from 0 to 100.
static const MInt32 ABS_KEY_SHAPE_NOSE_HIGHLIGHT					= 0x02040603;
// Feature key for intensities of Face Slender . Intensity from -100 to 100.
static const MInt32 ABS_KEY_SHAPE_FACE_SLENDER						= 0x02041401;
// Feature key for type of skin soften. 0 stands for QUALITY,1 stands for performance
static const MInt32 ABS_KEY_SKIN_SOFTEN_TYPE						= 0x10000001;

// only image
// Feature key for enable(level > 0) or disable status of De-Blemish.
static const MInt32 ABS_KEY_BASIC_DE_BLEMISH						= 0x02010101;
// Feature key for De-Blemish Beard Protection(level > 0) or not.
static const MInt32 ABS_KEY_BASIC_BEARD_PROTECTION					= 0x02010103;
// Feature key for intensities of Eye Brighten. Intensity from 0 to 100.
static const MInt32 ABS_KEY_BASIC_EYE_BRIGHTEN						= 0x02010202;
// Feature key for intensities of Pupil Brighten. Intensity from 0 to 100.
static const MInt32 ABS_KEY_BASIC_PUPIL_BRIGHTEN					= 0x02010203;
// Feature key for intensities of De-Pouch. Intensity from 0 to 100.
static const MInt32 ABS_KEY_BASIC_DE_POUCH							= 0x02010501;
// Feature key for enable(level > 0) or disable status of skin soften quickly mode for capture.
static const MInt32 ABS_KEY_SKIN_SOFTEN_QUICK_MODE					= 0x02010b0b;
// Feature key for enable(level > 0) or disable status of cut down input image size.
static const MInt32 ABS_KEY_RESIZE_QUICK_MODE						= 0x02010b0c;
// Feature key for intensities and color of Foundation. Intensity from 0 to 100.
static const MInt32 ABS_KEY_BASIC_FOUNDATION						= 0x02010c01;
// Feature key for intensity of make up. Intensity from 0 to 100.
static const MInt32 ABS_KEY_MAKEUP_ALL_LEVEL						= 0x02020a01;

// End of feature key definitions

typedef struct _tag_ABS_TFaces {
	MRECT		prtFaces[ABS_MAX_FACE_NUM];							// The array of face rectangles
	MUInt32		lFaceNum;											// Number of faces array
	MInt32		plFaceRoll[ABS_MAX_FACE_NUM];						// The roll angle of each face, between [0, 360)
} ABS_TFaces;

#endif /* _ABS_TYPES_H_ */
