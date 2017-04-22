/*
 *     This file is part of Discord4J.
 *
 *     Discord4J is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Lesser General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     Discord4J is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Lesser General Public License for more details.
 *
 *     You should have received a copy of the GNU Lesser General Public License
 *     along with Discord4J.  If not, see <http://www.gnu.org/licenses/>.
 */

package sx.blah.discord.api.internal;

import com.sun.jna.ptr.PointerByReference;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;

public class OpusUtil {

	public static final int OPUS_SAMPLE_RATE = 48000;
	public static final int OPUS_FRAME_SIZE = 960;
	public static final int OPUS_FRAME_TIME = 20;

	public static PointerByReference newEncoder(int channels) {
		return Opus.INSTANCE.opus_encoder_create(OPUS_SAMPLE_RATE, channels, Opus.OPUS_APPLICATION_AUDIO, IntBuffer.allocate(4));
	}

	public static PointerByReference newDecoder(int channels) {
		return Opus.INSTANCE.opus_decoder_create(OPUS_SAMPLE_RATE, channels, IntBuffer.allocate(4));
	}

	public static byte[] encode(PointerByReference encoder, byte[] pcm) {
		ShortBuffer nonEncodedBuffer = ShortBuffer.allocate(pcm.length / 2);
		ByteBuffer encodedBuffer = ByteBuffer.allocate(4096);
		for (int i = 0; i < pcm.length; i += 2) {
			int firstByte = (0x000000FF & pcm[i]);
			int secondByte = (0x000000FF & pcm[i + 1]);

			short combined = (short) ((firstByte << 8) | secondByte);
			nonEncodedBuffer.put(combined);
		}
		nonEncodedBuffer.flip();

		int result = Opus.INSTANCE.opus_encode(encoder, nonEncodedBuffer, OPUS_FRAME_SIZE, encodedBuffer, encodedBuffer.capacity());

		byte[] encoded = new byte[result];
		encodedBuffer.get(encoded);
		return encoded;
	}

	public static byte[] decode(PointerByReference decoder, byte[] opus) {
		ShortBuffer decodedBuffer = ShortBuffer.allocate(4096);

		int result = Opus.INSTANCE.opus_decode(decoder, opus, opus.length, decodedBuffer, OPUS_FRAME_SIZE, 0);

		short[] shortAudio = new short[result * 2];
		decodedBuffer.get(shortAudio);

		ByteBuffer byteBuffer = ByteBuffer.allocate(shortAudio.length * 2);
		byteBuffer.order(ByteOrder.BIG_ENDIAN);
		byteBuffer.asShortBuffer().put(shortAudio);
		return byteBuffer.array();
	}
}
