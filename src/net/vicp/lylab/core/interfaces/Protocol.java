package net.vicp.lylab.core.interfaces;

public interface Protocol {
	public byte[] getHead();

	/**
	 * Encode an object into byte stream by specific protocol
	 * @param obj
	 * @return
	 * The byte stream you need
	 * @throws LYException If any reason caused failure
	 */
	public byte[] encode(Object obj);

	/**
	 * Encode these bytes into objects defined by protocol
	 * 
     * <pre>
     * public Object decode(byte[] bytes) {
     *     return decode(bytes, 0);
     * } </pre>
     * 
	 * @param bytes source
	 * @return
	 * An object, null may means failed
	 * @throws LYException This contains information about why it fails
	 */
	public Object decode(byte[] bytes);

	/**
	 * Encode these bytes into objects defined by protocol
     * 
	 * @param bytes source
	 * @param offset encode from offset
	 * @return
	 * An object, null may means failed
	 * @throws LYException This contains information about why it fails
	 */
	public Object decode(byte[] bytes, int offset);

	/**
	 * Validate if these bytes could be assemble into a protocol
	 * 
     * <pre>
     * public int validate(byte[] bytes, int len) {
     *     return int validate(bytes, 0, len);
     * } </pre>
     * 
	 * @param bytes source
	 * @param len read length
	 * @return
	 * 0 not enough data<br>>0 index of the end position and of course validate passed
	 * @throws LYException If data is corrupt
	 */
	public int validate(byte[] bytes, int len);
	
	/**
	 * Validate from bytes if a specific position contains a protocol
	 * @param bytes source
	 * @param offset validate from offset
	 * @param len read length
	 * @return
	 * 0 not enough data<br>>0 index of the end position and of course validate passed
	 * @throws LYException If data is corrupt
	 */
	public int validate(byte[] bytes, int offset, int len);

}
