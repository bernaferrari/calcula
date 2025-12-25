package jscl.mathml

/**
 * A simple MathML document builder that creates XML content.
 * This is a KMP-compatible implementation using pure Kotlin.
 */
class MathML private constructor(private val node: MathMLNode) {

    /**
     * Creates a new MathML document with the specified DOCTYPE.
     */
    constructor(qualifiedName: String, publicID: String, systemID: String) : this(
        DocumentNode(qualifiedName, publicID, systemID)
    )

    /**
     * Creates a new element with the given name.
     */
    fun element(name: String): MathML = MathML(ElementNode(name))

    /**
     * Sets an attribute on this element.
     */
    fun setAttribute(name: String, value: String) {
        val element = node as? ElementNode
            ?: throw IllegalStateException("Cannot set attribute on non-element node")
        element.attributes[name] = value
    }

    /**
     * Creates a text node with the given data.
     */
    fun text(data: String): MathML = MathML(TextNode(data))

    /**
     * Appends a child node to this element.
     */
    fun appendChild(child: MathML) {
        when (val n = node) {
            is DocumentNode -> n.children.add(child.node)
            is ElementNode -> n.children.add(child.node)
            is TextNode -> throw IllegalStateException("Cannot append child to text node")
        }
    }

    override fun toString(): String {
        val sb = StringBuilder()
        when (val n = node) {
            is DocumentNode -> {
                // Write children (skip DOCTYPE for output compatibility with original)
                for (child in n.children) {
                    writeNode(sb, child)
                }
            }
            is ElementNode -> writeNode(sb, n)
            is TextNode -> sb.append(escapeXml(n.data))
        }
        return sb.toString()
    }

    private fun writeNode(sb: StringBuilder, node: MathMLNode) {
        when (node) {
            is ElementNode -> {
                sb.append('<').append(node.name)
                for ((attrName, attrValue) in node.attributes) {
                    sb.append(' ').append(attrName).append("=\"")
                    sb.append(escapeXmlAttr(attrValue)).append('"')
                }
                if (node.children.isEmpty()) {
                    sb.append("/>")
                } else {
                    sb.append('>')
                    for (child in node.children) {
                        writeNode(sb, child)
                    }
                    sb.append("</").append(node.name).append('>')
                }
            }
            is TextNode -> {
                sb.append(escapeXml(node.data))
            }
            is DocumentNode -> {
                for (child in node.children) {
                    writeNode(sb, child)
                }
            }
        }
    }

    private fun escapeXml(text: String): String {
        val sb = StringBuilder(text.length)
        for (c in text) {
            when (c) {
                '<' -> sb.append("&lt;")
                '>' -> sb.append("&gt;")
                '&' -> sb.append("&amp;")
                else -> sb.append(c)
            }
        }
        return sb.toString()
    }

    private fun escapeXmlAttr(text: String): String {
        val sb = StringBuilder(text.length)
        for (c in text) {
            when (c) {
                '<' -> sb.append("&lt;")
                '>' -> sb.append("&gt;")
                '&' -> sb.append("&amp;")
                '"' -> sb.append("&quot;")
                else -> sb.append(c)
            }
        }
        return sb.toString()
    }
}

/**
 * Sealed class hierarchy for MathML nodes.
 */
private sealed class MathMLNode

private class DocumentNode(
    val qualifiedName: String,
    val publicID: String,
    val systemID: String
) : MathMLNode() {
    val children = mutableListOf<MathMLNode>()
}

private class ElementNode(val name: String) : MathMLNode() {
    val attributes = mutableMapOf<String, String>()
    val children = mutableListOf<MathMLNode>()
}

private class TextNode(val data: String) : MathMLNode()
